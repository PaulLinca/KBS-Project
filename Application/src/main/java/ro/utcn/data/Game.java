package ro.utcn.data;

import java.util.*;

public class Game
{
    public static Game instance = null;

    List<String> possibleCharacters;
    List<Filter> filters;
    public SparqlQuery characterQuery = new SparqlQuery();
    public Filter currentFilter;
    public String currentQuestion;
    public int currentQuestionNumber;
    public int questionListIterator;
    public Map<Integer, String> questionsMap = new HashMap<>();
    public boolean isGameFinished = false;

    public String finalGuess = "";

    private Game()
    {
        int indexInMap = -1;
        filters = new ArrayList<>();

        filters.add(new Filter("rdf:type", "dbo:Person"));
        questionsMap.put(++indexInMap, "a person");
        filters.add(new Filter("foaf:gender", "\"female\"@en"));
        questionsMap.put(++indexInMap, "a female");
        filters.add(new Filter("dbp:publisher", "dbr:Marvel_Comics"));
        questionsMap.put(++indexInMap, "published by Marvel");
        filters.add(new Filter("dct:subject", "dbc:S.H.I.E.L.D._agents"));
        questionsMap.put(++indexInMap, "a S.H.I.E.L.D agent");
        filters.add(new Filter(" dbp:homeworld", "dbr:Earth"));
        questionsMap.put(++indexInMap, "living on Earth");
        filters.add(new Filter("dct:subject", "dbc:Marvel_Comics_characters_who_use_magic"));
        questionsMap.put(++indexInMap, "a magic user");
        filters.add(new Filter("dbp:publisher", "dbr:DC_Comics"));
        questionsMap.put(++indexInMap, "published by DC Comics");
        filters.add(new Filter("dbp:alliances", "dbr:Justice_League"));
        questionsMap.put(++indexInMap, "a member of the Justice League");

        currentQuestionNumber = 1;
        questionListIterator = 0;
        currentFilter = filters.get(questionListIterator);
        currentQuestion = "Is your character " + questionsMap.get(questionListIterator) + " ?";
    }

    public static Game getInstance()
    {
        if (instance == null)
        {
            instance = new Game();
        }

        return instance;
    }

    private void checkGameFinished()
    {
        if(isGameFinished)
        {
            return;
        }

        if(possibleCharacters.size() == 0)
        {
            guess();
            isGameFinished =  true;
        }

        if(possibleCharacters.size() == 1)
        {
            guess();
            isGameFinished = true;
        }

        if(currentQuestionNumber == 20)
        {
            guess();
            isGameFinished =  true;
        }

        if(questionListIterator >= filters.size())
        {
            guess();
            isGameFinished =  true;
        }
    }

    private void updateGameParameters()
    {
        questionListIterator++;
        if(questionListIterator < filters.size())
        {
            currentFilter = filters.get(questionListIterator);
            currentQuestion = "Is your character " + questionsMap.get(questionListIterator) + "?";
        }
        else
        {
            checkGameFinished();
        }
    }

    private void addFilterToQuery(boolean isPositive)
    {
        if (isPositive)
        {
            characterQuery.addFilter(currentFilter.positiveFilter());
        }
        else
        {
            characterQuery.addFilter(currentFilter.negativeFilter());
        }
    }

    private void findNextQuestion()
    {
        while(!isGameFinished)
        {
            possibleCharacters= characterQuery.getQueryResultPages();

            boolean isNextQuestionApplicable = false;
            int listIterator = 0;
            while(!isNextQuestionApplicable && listIterator < possibleCharacters.size())
            {
                isNextQuestionApplicable = SparqlQuery.executeBooleanQuery(possibleCharacters.get(listIterator), currentFilter.attribute, currentFilter.value);
                listIterator++;
            }

            if(!isNextQuestionApplicable)
            {
                updateGameParameters();
            }
            else
            {
                checkGameFinished();
                break;
            }
        }
    }

    public void generateNextQuestion(boolean answer)
    {
        addFilterToQuery(answer);
        currentQuestionNumber++;
        updateGameParameters();
        findNextQuestion();
    }

    private void guess()
    {
        if(possibleCharacters.size() == 0)
        {
            finalGuess = "I COULDN'T GUESS YOUR CHARACTER :(((((";
        }
        else
        {
            finalGuess = possibleCharacters.get(0);
        }
        System.out.println(finalGuess);
    }

    public static void reset()
    {
        instance = new Game();
    }
}
