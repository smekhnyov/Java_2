import java.io.IOException;
import java.util.Scanner;

public class ExpressionParser
{
    // Лексемы элементов выражения
    final int NONE = 0; // Пустой
    final int SEP = 1; // Разделитель (знаки операций и скобки)
    final int VAR = 2; // Переменная
    final int NUM = 3; // Число

    // Лексемы синтаксических ошибок
    final int SYNTAXERROR = 0; // Синтаксическая ошибка
    final int BRACKETNUM = 1; // Несовпадение количества открытых и закрытых скобок
    final int NOEXPRESSION = 2; // Отсутствие выражения
    final int DIVIDEBYZERO = 3; // Деление на ноль

    final String EOX = "\0"; // Лексема для конца выражения

    private String expression; // Строка с выражением
    private int currentIndex; // Текущий индекс выражения
    private String token; // Текущая лексема
    private int tokenType; // Тип текущей лексемы

    public String toString()
    {
        return String.format("Выражение: {0}\nИндекс: {1}\nЛексема: {2}\nТип лексемы: {3}\n",
                expression.toString(), currentIndex, token.toString(), tokenType);
    }

    private boolean isSeparator(char character)
    {
        return (" +-/*^=()".indexOf(character)) != -1;
    }

    private void getNextToken()
    {
        token = "";
        tokenType = NONE;

        if (currentIndex == expression.length())
        {
            token = EOX;
            return;
        }
        while (currentIndex < expression.length() && Character.isWhitespace(expression.charAt(currentIndex)))
        {
            currentIndex++;
        }
        if (currentIndex == expression.length())
        {
            token = EOX;
            return;
        }

        if (isSeparator(expression.charAt(currentIndex)))
        {
            token += expression.charAt(currentIndex);
            currentIndex++;
            tokenType = SEP;
        }
        else if (Character.isLetter(expression.charAt(currentIndex)))
        {
            while (!isSeparator(expression.charAt(currentIndex)))
            {
                token += expression.charAt(currentIndex);
                currentIndex++;
                if (currentIndex >= expression.length())
                {
                    break;
                }
            }
            tokenType = VAR;
        }
        else if (Character.isDigit(expression.charAt(currentIndex)))
        {
            while (!isSeparator(expression.charAt(currentIndex)))
            {
                token += expression.charAt(currentIndex);
                currentIndex++;
                if (currentIndex >= expression.length())
                {
                    break;
                }
            }
            tokenType = NUM;
        }
        else
        {
            token = EOX;
            return;
        }
    }

    public double calculate(String expression) throws ParserException
    {
        double result;
        this.expression = expression;
        currentIndex = 0;
        getNextToken();
        if (token.equals(EOX))
        {
            handleError(NOEXPRESSION);
        }

        result = calculateAddOrSubtract();

        if (!token.equals(EOX))
        {
            handleError(SYNTAXERROR);
        }

        return result;
    }

    private double calculateAddOrSubtract() throws ParserException
    {
        char operation;
        double result;
        double partialResult;
        result = calculateMultiplyOrDivide();
        while ((operation = token.charAt(0)) == '+' || operation == '-')
        {
            getNextToken();
            partialResult = calculateMultiplyOrDivide();
            switch (operation)
            {
                case '-':
                    result -= partialResult;
                    break;
                case '+':
                    result += partialResult;
                    break;
            }
        }
        return result;
    }

    private double calculateMultiplyOrDivide() throws ParserException
    {
        char operation;
        double result;
        double partialResult;
        result = calculateGrading();
        while ((operation = token.charAt(0)) == '*' || operation == '/')
        {
            getNextToken();
            partialResult = calculateGrading();
            switch (operation)
            {
                case '*':
                    result *= partialResult;
                    break;
                case '/':
                    if (partialResult == 0.0)
                    {
                        handleError(DIVIDEBYZERO);
                    }
                    result /= partialResult;
                    break;
            }
        }
        return result;
    }

    private double calculateGrading() throws ParserException
    {
        double result;
        double partialResult;
        double grade;
        int t;
        result = calculateUnary();
        if (token.equals("^"))
        {
            getNextToken();
            partialResult = calculateGrading();
            grade = result;
            if (partialResult == 0.0)
            {
                result = 1.0;
            }
            else
            {
                for (t = (int)partialResult - 1; t > 0; t--)
                {
                    result *= grade;
                }
            }
        }
        return result;
    }

    private double calculateUnary() throws ParserException
    {
        double result;
        String operation = " ";
        if ((tokenType == SEP) && token.equals("+") || token.equals("-"))
        {
            operation = token;
            getNextToken();
        }
        result = calculateBracket();
        if (operation.equals("-"))
        {
            result = -result;
        }
        return result;
    }

    private double calculateBracket() throws ParserException
    {
        double result;
        if (token.equals("("))
        {
            getNextToken();
            result = calculateAddOrSubtract();
            if (!token.equals(")"))
            {
                handleError(BRACKETNUM);
            }
            getNextToken();
        }
        else
        {
            result = getValue();
        }
        return result;
    }

    private double getValue() throws ParserException
    {
        double result = 0.0;
        switch (tokenType)
        {
            case NUM:
                try
                {
                    result = Double.parseDouble(token);
                }
                catch (NumberFormatException exp)
                {
                    handleError(SYNTAXERROR);
                }
                getNextToken();
                break;
            case VAR:
                Scanner in = new Scanner(System.in);
                System.out.printf("Значение %s:", token);
                try
                {
                    result = in.nextDouble();
                }
                catch (Exception error)
                {
                    error.printStackTrace();
                }
                getNextToken();
                break;
            default:
                handleError(SYNTAXERROR);
                break;
        }
        return result;
    }

    private void handleError(int EXCEPTION) throws ParserException
    {
        String[] errors = {"Синтаксическая ошибка", "Несоответствие скобок", "Нет выражения", "Деление на ноль"};
        throw new ParserException(errors[EXCEPTION]);
    }
}
