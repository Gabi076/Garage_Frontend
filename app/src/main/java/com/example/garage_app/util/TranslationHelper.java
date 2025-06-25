package com.example.garage_app.util;

import com.example.garage_app.model.Car;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.concurrent.atomic.AtomicInteger;

public class TranslationHelper {
    private Translator translator;

    public TranslationHelper() {
    }

    public void translateText(String text, TranslationCallback callback) {

        if (text == null || text.trim().isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Textul de tradus este null sau gol"));
            return;
        }

        prepareTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.ROMANIAN);
        performTranslation(text, callback);
    }

    public void translateCarFields(Car car, CarTranslationCallback callback) {
        StringBuilder errorMessages = new StringBuilder();
        AtomicInteger pendingTranslations = new AtomicInteger(3);
        TranslationCallback fieldCallback = new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
            }

            @Override
            public void onFailure(Exception e) {
                errorMessages.append(e.getMessage()).append("\n");
                if (pendingTranslations.decrementAndGet() == 0) {
                    if (errorMessages.length() > 0) {
                        callback.onFailure(new Exception(errorMessages.toString()));
                    } else {
                        callback.onSuccess(car);
                    }
                }
            }
        };

        translateText(car.getDrive(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setDrive(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });

        translateText(car.getTransmission(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setTransmission(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });

        translateText(car.getEngineType(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setEngineType(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });

        translateText(car.getCylinders(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setCylinders(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });

        translateText(car.getDisplacement(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setDisplacement(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });

        translateText(car.getFuelType(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setFuelType(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });

        translateText(car.getDoors(), new TranslationCallback() {
            @Override
            public void onSuccess(String translatedText) {
                car.setDoors(translatedText);
                if (pendingTranslations.decrementAndGet() == 0) callback.onSuccess(car);
            }

            @Override
            public void onFailure(Exception e) {
                fieldCallback.onFailure(e);
            }
        });
    }

    public interface CarTranslationCallback {
        void onSuccess(Car translatedCar);
        void onFailure(Exception e);
    }

    private void prepareTranslator(String sourceLanguage, String targetLanguage) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build();

        if (translator != null) {
            translator.close();
        }
        translator = Translation.getClient(options);
    }

    private void performTranslation(String text, TranslationCallback callback) {
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(v -> {
                    translator.translate(text)
                            .addOnSuccessListener(callback::onSuccess)
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void cleanup() {
        if (translator != null) {
            translator.close();
        }
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onFailure(Exception e);
    }
}
