package pl.curlycode.herbymazowszaquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivityFragment extends Fragment {

    private static final String TAG = "HerbyMazowszaQuiz: ";
    private static final int FLAGS_IN_QUIZ = 5;

    private List<String> fileNameList;
    private List<String> quizRegionsList;
    private Set<String> regionSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;
    private LinearLayout quizLinearLayout;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private LinearLayout[] guessLinearLayouts;
    private TextView answerTextView;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fileNameList = new ArrayList<>();
        quizRegionsList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1linearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2linearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3linearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4linearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
        questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));
        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {
        String choices = sharedPreferences.getString(MainActivity.CHOICES,null);
        guessRows = Integer.parseInt(choices) / 2;
        for (LinearLayout layout : guessLinearLayouts) {
            layout.setVisibility(View.GONE);
        }

        for (int row = 0; row < guessRows; row++) {
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
        }
    }

    public void updateRegions(SharedPreferences sharedPreferences) {
        regionSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    public void resetQuiz() {
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            for (String region : regionSet) {
                String[] paths = assets.list(region);

                for (String path : paths) {
                    fileNameList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Blad ladowania obrazow herbow", ex);
        }

        correctAnswers = 0;
        totalGuesses = 0;
        quizRegionsList.clear();
        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        while (flagCounter <= FLAGS_IN_QUIZ) {

            int randomIndex = random.nextInt(numberOfFlags);
            String fileName = fileNameList.get(randomIndex);

            if (!quizRegionsList.contains(fileName)) {
                quizRegionsList.add(fileName);
                ++flagCounter;
            }
        }
        loadNextFlag();
    }

    private void loadNextFlag() {

        String nextImage = quizRegionsList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText("");
        questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), FLAGS_IN_QUIZ));
        String region = nextImage.substring(0, nextImage.indexOf("-"));
        AssetManager assets = getActivity().getAssets();

        try (InputStream inputStreamFlag = assets.open(region + "/" + nextImage + ".png")) {
            Drawable drawableFlag = Drawable.createFromStream(inputStreamFlag, nextImage);
            flagImageView.setImageDrawable(drawableFlag);
            animate(false);
        } catch (IOException ex) {
            Log.e(TAG, "Blad podczas ladowania" + nextImage, ex);
        }

        Collections.shuffle(fileNameList);

        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        for (int row = 0; row < guessRows; row++) {
            for (int column = 0; column < 2; column++) {
                Button guessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                guessButton.setEnabled(true);

                String fileName = fileNameList.get((row * 2) + column);
                guessButton.setText(getRegionName(fileName));
            }
        }

        int row = random.nextInt(guessRows);
        int column = random.nextInt(2);
        LinearLayout randomRow = guessLinearLayouts[row];
        String regionName = getRegionName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(regionName);
    }

    private  String getRegionName(String name) {
        return name.substring(name.indexOf("-") + 1).replace("_"," ");
    }

    private void animate(boolean animateOut) {
        if (correctAnswers == 0) return;

        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom()) / 2;
        int radius  = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());

        Animator animator;

        if (animateOut) {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });
        } else {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,0,radius);
        }

        animator.setDuration(500);
        animator.start();
    }


    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Button guessButton = (Button) v;
            String guess = guessButton.getText().toString();
            String answer = getRegionName(correctAnswer);

            ++totalGuesses;

            if (guess.equals(answer)) {
                    ++correctAnswers;
                    answerTextView.setText(answer + "!");
                    answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));
                    disableButtons();

                    if (correctAnswers == FLAGS_IN_QUIZ) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(("Quiz results"));
                        builder.setMessage(getString(R.string.results, totalGuesses, (500 / (double) totalGuesses)));
                        builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                resetQuiz();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();

                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animate(true);
                            }
                        }, 2000);
                    }

            } else {
                flagImageView.startAnimation(shakeAnimation);
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);
            }
        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int column = 0; column < 2; column++) {
                guessRow.getChildAt(column).setEnabled(false);
            }
        }
    }
}
