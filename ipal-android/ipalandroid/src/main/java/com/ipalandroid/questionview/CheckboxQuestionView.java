package com.ipalandroid.questionview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ipalandroid.R;
import com.ipalandroid.common.ImageDownloader;
import com.ipalandroid.common.TouchImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents Checkbox Question View. Any question with checkboxes use this java and activity.
 * This currently is only multichoice with more than one answer. The class creates the view from HTML,
 * validates input and send answers to the server
 *
 * @author Ngoc Nguyen modified by William Junnkin
 *
 */

public class CheckboxQuestionView extends QuestionView {
    private String TAG = "CheckboxQuestionView";
    /**
     * This class represents Choice in Multiple Choice question with several possible answers.
     * Each choice text and a value.
     *
     * @author Ngoc Nguyen modified by William Junkin
     *
     */
    private class Choice
    {
        private String text;
        private int value;

        public Choice(String cText, int cValue) {
            text= cText;
            value= cValue;
        }

        /**
         * @return the text of the current choice
         */
        public String getText() { return text; }

        /**
         * @return the value of the current choice
         */
        public int getValue() { return value; }
    }

    private ArrayList<CheckboxQuestionView.Choice> choices = new ArrayList<CheckboxQuestionView.Choice>();
    private String currentChoice;
    // The array of anser ids that have been cheecked. When one is checked it is added to the array; when unchecked, removed.
    ArrayList<String> selection = new ArrayList<String>();
    public void setArray(String myid) {
        if (selection.contains(myid)) {
            selection.remove(myid);
        } else {
            selection.add(myid);
        }
        String current_selections = "";
        for (String Selections: selection) {
            current_selections = current_selections + Selections + "";
        }
        Log.d("ON_CLICK", current_selections);
    }


    /**
     * A constructor for the MultipleChoiceQuestionView. It gets the question text,
     * populates the choices, prepares Moodle url and username for submission.
     *
     * @param questionPage: the JSoup document fetched from the server
     * @param url: Moodle URL
     * @param username: Moodle User Name
     * @param passcode: ipal Passcode
     */
    public CheckboxQuestionView(Document questionPage, String url, String username, int passcode)
    {
        super(questionPage, url, username, passcode);
        questionPage = this.questionPage;
        //Populates the choices
        Log.d(TAG,"Line 84");
        Integer i = 0;
        Elements spans = this.questionPage.getElementsByTag("span");
        for (Element s: spans) {
            String cText = s.select("label").text();
            Log.d(TAG, "Line 94 and label is "+cText);
            if (s.select("input").attr("value").length() > 0) {
                int cValue = Integer.parseInt(s.select("input").attr("value"));
                choices.add(new Choice(cText, cValue));
            }

        }
        //Set the question Text
        qText = this.questionPage.select("legend").text();
        //Set the Image URL if there is one
        imageURL = questionPage.select("img").attr("src");
        //Log.w("IMAGE URL", imageURL+ "   a a a");
    }

    @Override
    public View getQuestionView(Context c) {
        Log.d(TAG,"Line 115");

        LinearLayout linearcheckbox;
        CheckBox mycheckbox;
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //ScrollView view = (ScrollView) inflater.inflate(R.layout.multichoice, null);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.checkbox,null);
        TextView qTextView = (TextView) layout.findViewById(R.id.questionText);
        qText = this.questionPage.select("legend").text();
        qTextView.setText(qText);
        Log.d(TAG,"Line 123"+qText);
        linearcheckbox = (LinearLayout) layout.findViewById(R.id.layout);

        for (Choice ch: choices) {
            mycheckbox = new CheckBox(c);
            mycheckbox.setText(ch.text);
            mycheckbox.setId(ch.value);
            mycheckbox.setTextColor(c.getResources().getColor(R.color.view_text_color));
            mycheckbox.setOnClickListener(getOnClickDoSomething(mycheckbox));
            //b.setTextColor(R.color.view_text_color);
            linearcheckbox.addView(mycheckbox);
        }
        return layout;
    }
    View.OnClickListener getOnClickDoSomething(final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String myButtonId;
                myButtonId = button.getId()+"";
                Log.d("ON_CLICK", "Checkbox ID:"+button.getId() + "Text" + button.getText().toString());
                setArray(myButtonId);
            }


        };
    }

    @Override
    public Boolean validateInput() {
        return true;
    }

    @Override
    public Boolean sendResult() {
        // I am sending all responses in the URL.

        if (!(selection.isEmpty())) {
            String myurl = url+"mod/ipal/tempview.php?user="+username+"&p="+passcode;
            String aid = "&answers_id[]=";
            String mychoice = "";
            for (String Selections: selection){
                myurl = myurl+aid+Selections;
                //mychoice = Selections+"";
            }
            Log.d("ON_CLICK",myurl);
            try {
                Jsoup.connect(myurl)
                        .data("answer_id", -2+"")
                        //.data("answers_id[]",mychoice+"")
                        .data("a_text", "")
                        .data("question_id", question_id+"")
                        .data("active_question_id", active_question_id+"")
                        .data("course_id", course_id+"")
                        .data("user_id", user_id+"")
                        .data("submit", "Submit")
                        .data("ipal_id", ipal_id+"")
                        .data("instructor", instructor)
                        .post();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
/**
    private int getChoiceValueFromText(String cText) {
        for (Choice c: choices) {
            if (c.getText().equals(cText)) {
                return c.getValue();
            }
        }
        return -1;
    }
**/
    @Override
    public LinearLayout getLayout() {
        // TODO Auto-generated method stub
        return null;
    }

}
