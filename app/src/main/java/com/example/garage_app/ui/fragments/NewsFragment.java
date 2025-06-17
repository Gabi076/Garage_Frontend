package com.example.garage_app.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garage_app.adapter.NewsAdapter;
import com.example.garage_app.api.NewsApiService;
import com.example.garage_app.model.Article;
import com.example.garage_app.model.NewsResponse;
import com.example.garage_app.util.NewsApiClient;
import com.example.garage_app.R;


import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView noNewsText;
    private NewsAdapter adapter;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        recyclerView = view.findViewById(R.id.recycler_news);
        noNewsText = view.findViewById(R.id.no_news_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchCarNews("(cars OR automotive)");

        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchQuery = "(cars OR automotive) AND " + query;
                fetchCarNews(searchQuery);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;
    }

    private void fetchCarNews(String query) {
        NewsApiService apiService = NewsApiClient.getApiService();
        Call<NewsResponse> call = apiService.getCarNews(
                query,
                "en",
                "c0dc2df639ca42e2bd7f2e5c372684d1"
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = response.body().getArticles();

                    for (Article article : articles) {
                        Log.d("NEWS_API", "Articol: " + article.getTitle() + ", Imagine: " + article.getUrlToImage());
                    }

                    if (articles.isEmpty()) {
                        noNewsText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noNewsText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                    adapter = new NewsAdapter(articles, article -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                    }
                } else {
                    Log.e("NEWS_API", "Eroare API: " + response.code() + " - " + response.message());
                    noNewsText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("NEWS_API", "Eroare rețea: " + t.getMessage());
                noNewsText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }
}
