#include <iostream>
#include <cstdio>
#include <cstdlib>
#include <vector>
#include <string>
#include <map>
#include <algorithm>
#include "lexer.h"
#include "parser.h"

using namespace std;
vector<string> nonterm;
vector<string> terms;
vector<string> lhs;
vector<Rules> rhs;
vector<string> nullSet;
vector<vector<string>> firstSets;
vector<vector<string>> followSets;
struct Rules currentRule;

void Parser::syntax_error()
{
    cout << "SYNTAX ERROR !!!!!!!!!!!!!!\n";
    exit(1);
}

Token Parser::expect(TokenType expected_type)
{
    Token t = lexer.GetToken();
    if (t.token_type != expected_type)
        syntax_error();
    return t;
}

void Parser::parseGrammar()
{
    parse_rule_list();
    expect(HASH);
}

void Parser::parse_rule_list()
{
    parse_rule();
    Token current = lexer.peek(1);
    if (current.token_type == ID)
    {
        parse_rule_list();
    }
}

void Parser::parse_rule()
{
    lhs.push_back(expect(ID).lexeme);
    expect(ARROW);
    parse_RHS();
    expect(STAR);
}

void Parser::parse_RHS()
{
    parse_IDL();
    Token current = lexer.peek(1);
    if (current.token_type == OR || current.token_type == STAR)
    {
        if (current.token_type == OR)
        {
            expect(OR);
            parse_RHS();
        }
        else
        {
            rhs.push_back(currentRule);
            currentRule.rule.clear();
        }
    }
}

void Parser::parse_IDL()
{
    Token current = lexer.peek(1);
    if (current.token_type == STAR || current.token_type == OR)
    {
        currentRule.rule.push_back(vector<string>());
    }
    else
    {
        vector<string> fullrule;
        while (current.token_type != STAR && current.token_type != OR)
        {
            fullrule.push_back(expect(ID).lexeme);
            current = lexer.peek(1);
        }
        currentRule.rule.push_back(fullrule);
    }
}

void Parser::ReadGrammar() 
{
    parseGrammar();
    /* Task 1 set up, we will also use this information for future tasks, which is why we do it in the ReadGrammar method.
    * We first check if there's any duplicates within the nonterm array. If there is not, we push the non terminal into the nonterm array (from the LHS reading)
    * However, we must first check if we see the nonterminal anywhere before it is declared in a rule.
    * We have a boolean to tell us if the ID we are reading is seen anywhere within the LHS array.
    * We then look at one specific ID at a time, and we check if this ID is anywhere in the LHS array. If it is, we set the boolean to true.
    * We check if the ID is already in the non terminal list (aka, dupes). If so, we don't add it, but if it isn't, we add it.
    * If the ID never appears on the right side, we do the same dupe checking, except for the terminal list. 
    * 
    */
    for (int i = 0; i < lhs.size(); i++) 
    {
        bool dupe = false;
        for (int l = 0; l < nonterm.size(); l++)
        {
            if (lhs.at(i) == nonterm.at(l))
            {
                dupe = true;
            }
        }
        if (!dupe)
        {
            nonterm.push_back(lhs.at(i));
        }
        for (int j = 0; j < rhs.at(i).rule.size(); j++)
        {
            for (int m = 0; m < rhs.at(i).rule.at(j).size(); m++)
            {
                bool onleft = false;
                string toComp = rhs.at(i).rule.at(j).at(m);
                for (int k = 0; k < lhs.size(); k++)
                {
                    if (lhs.at(k) == toComp)
                    {
                        onleft = true;
                    }
                }
                if (onleft)
                {
                    bool checkdupe = false;
                    for (int o = 0; o < nonterm.size(); o++)
                    {
                        if (toComp == nonterm.at(o))
                        {
                            checkdupe = true;
                        }
                    }
                    if (!checkdupe)
                    {
                        nonterm.push_back(toComp);
                    }
                }
                else
                {
                    bool checkdupe = false;
                    for (int q = 0; q < terms.size(); q++)
                    {
                        if (toComp == terms.at(q))
                        {
                            checkdupe = true;
                        }
                    }
                    if (!checkdupe)
                    {
                        terms.push_back(toComp);
                    }
                }
            }
        }
    }
    /* Task 2 set up. We set up this task here since, again, we will be using it for the rest of the other tasks as well.
    * The easiest way to check for intial nullables is to check the rules. We check all the rules for a given LHS variable/ID.
    * If any of those vectors are empty, this means that that ID is nullable, and we add it to the nullSet (given that there isn't a duplicate, which we check for)
    * Then, we need to recheck all rules/IDs if we ever add something to the nullSet, so we have a boolean to keep track of this (reCheck)
    * We first check to see if the LHS ID that we are dealing with is already in the nullSet, if so, we don't have to do anything. If not, we have to check if all the IDs that are within each rule of the LHS ID are in the nullSet (only one rule has to be nullable).
    * We have a boolean to make sure that all the symbols/IDS within a rule can be null.
    * Then, we check each ID within a rule. We have two booleans to help with checking. The first boolean is to see whether or not a non-terminal is within the null set. The second boolean is to see whether the ID we are looking at is a terminal.
    * If the ID we are looking at is a terminal, then we immediatly skip to end, since it can't be part of the nullSet if the rule contains a terminal. If we are looking at a non-terminal, we then check if that non-terminal is in the nullSet.
    * If that non-terminal is in the nullSet, we can then move on to the next ID, and if all IDs pass, we can add this non-terminal to the nullSet.
    */
    for (int i = 0; i < lhs.size(); i++)
    {
        for (int j = 0; j < rhs.at(i).rule.size(); j++)
        {
            if (rhs.at(i).rule.at(j).size() == 0)
            {
                bool dupe = false;
                for (int n = 0; n < nullSet.size(); n++)
                {
                    if (nullSet.at(n) == lhs.at(i))
                    {
                        dupe = true;
                        break;
                    }
                }

                if (!dupe)
                {
                    nullSet.push_back(lhs.at(i));
                }
                break;
            }
        }
    }

    bool reCheck = true;
    while (reCheck)
    {
        reCheck = false;

        for (int i = 0; i < lhs.size(); i++)
        {
            bool dupe = false;
            for (int n = 0; n < nullSet.size(); n++)
            {
                if (nullSet.at(n) == lhs.at(i))
                {
                    dupe = true;
                    break;
                }
            }

            if (!dupe)
            {
                for (int j = 0; j < rhs.at(i).rule.size(); j++)
                {
                    bool allNull = true;

                    for (int k = 0; k < rhs.at(i).rule.at(j).size(); k++)
                    {
                        string symbol = rhs.at(i).rule.at(j).at(k);

                        bool notNullset = false;

                        bool notATerminal = false;
                        for (int l = 0; l < nonterm.size(); l++)
                        {
                            if (nonterm.at(l) == symbol)
                            {
                                notATerminal = true;
                                break;
                            }
                        }

                        if (notATerminal)
                        {
                            for (int n = 0; n < nullSet.size(); n++)
                            {
                                if (nullSet.at(n) == symbol)
                                {
                                    notNullset = true;
                                    break;
                                }
                            }
                        }

                        if (!notNullset)
                        {
                            allNull = false;
                            break;
                        }
                    }

                    if (allNull)
                    {
                        nullSet.push_back(lhs.at(i));
                        reCheck = true;
                        break;
                    }
                }
            }
        }
    }

    /*
    * First and Follow sets will only be for nonterminals, so we resize the vector here.
    */
    firstSets.resize(nonterm.size());
    followSets.resize(nonterm.size());

    /* Task 3. Follow sets use first sets, so we need to (again), implement the task functionality up here.
    *  Before anything, we first want to get the initial first sets that include terminals (aka, any rules that start with a terminal, add that terminal to the respective non-terminal first set)
    *  We do this by checking each first vector/rule of each ID.
    *  If it's empty, we simply do nothing for now.
    *  If it's not empty, we must find out whether the first ID is a terminal, or non terminal.
    *  First, we must find where to place the first set (since it must be printed in the order in which it appears).
    *  If it is a terminal, we add that terminal to the first set (as long as there isn't a duplicate)
    *  If it is not a terminal, we simply deal with that in the next section.
    */
    for (int i = 0; i < lhs.size(); i++)
    {
        for (int j = 0; j < rhs.at(i).rule.size(); j++)
        {
            if (rhs.at(i).rule.at(j).empty())
            {
                i = i;
            }
            else
            {
                int nonTermIndex = -100;
                for (int k = 0; k < nonterm.size(); k++)
                {
                    if (nonterm.at(k) == lhs.at(i))
                    {
                        nonTermIndex = k;
                        break;
                    }
                }

                string checkFirst = rhs.at(i).rule.at(j).at(0);
                bool isTerminal = true;
                for (int k = 0; k < nonterm.size(); k++)
                {
                    if (nonterm.at(k) == checkFirst)
                    {
                        isTerminal = false;
                        break;
                    }
                }

                if (isTerminal)
                {
                    bool dupe = false;
                    for (int k = 0; k < firstSets.at(nonTermIndex).size(); k++)
                    {
                        if (firstSets.at(nonTermIndex).at(k) == checkFirst)
                        {
                            dupe = true;
                            break;
                        }
                    }

                    if (!dupe)
                    {
                        firstSets.at(nonTermIndex).push_back(checkFirst);
                    }
                }
            }
        }
    }
    /* Task 3 pt 2. 
    *  We need to recheck all first sets if we change anything. This is the same logic we used in task 2.
    *  We do a very similar sequence of events as above. (Logic for the first portion is found above).
    *  We then look at each rule, and each ID in each rule individually. If current ID we are looking at is a terminal, we add that terminal to the firstset of the nonterminal we are looking at (without duplicates of course).
    *  If we are looking at a non-terminal, we then must look at the first set of that specific non-terminal (so we store that into firstSetInd).
    *  We then add the first set to the first set of the current non-terminal (barring duplicates).
    *  However, if the non-terminal we are looking at is in the nullset, we can simply skip this symbol and move on to the next symbol instead.
    *  We then move on to the next first set.
    *  If anything changed after going through all the first sets, we go through the process again.
    */
    reCheck = true;
    while (reCheck)
    {
        reCheck = false;
        for (int i = 0; i < lhs.size(); i++)
        {
            int nonTermIndex = -1;
            for (int k = 0; k < nonterm.size(); k++)
            {
                if (nonterm.at(k) == lhs.at(i))
                {
                    nonTermIndex = k;
                    break;
                }
            }

            for (int j = 0; j < rhs.at(i).rule.size(); j++)
            {
                if (rhs.at(i).rule.at(j).empty())
                {
                    i = i;
                }

                else
                {
                    for (int k = 0; k < rhs.at(i).rule.at(j).size(); k++)
                    {
                        string current = rhs.at(i).rule.at(j).at(k);
                        bool isTerminal = true;
                        int firstSetInd = -100;
                        for (int l = 0; l < nonterm.size(); l++)
                        {
                            if (nonterm.at(l) == current)
                            {
                                isTerminal = false;
                                firstSetInd = l;
                                break;
                            }
                        }

                        if (isTerminal)
                        {
                            bool dupe = false;
                            for (int l = 0; l < firstSets.at(nonTermIndex).size(); l++)
                            {
                                if (firstSets.at(nonTermIndex).at(l) == current)
                                {
                                    dupe = true;
                                    break;
                                }
                            }

                            if (!dupe)
                            {
                                firstSets.at(nonTermIndex).push_back(current);
                                reCheck = true;
                            }
                            break;
                        }
                        else
                        {
                            for (int l = 0; l < firstSets.at(firstSetInd).size(); l++)
                            {
                                string firstSetSymbol = firstSets.at(firstSetInd).at(l);
                                bool dupe = false;
                                for (int m = 0; m < firstSets.at(nonTermIndex).size(); m++)
                                {
                                    if (firstSets.at(nonTermIndex).at(m) == firstSetSymbol)
                                    {
                                        dupe = true;
                                        break;
                                    }
                                }

                                if (!dupe)
                                {
                                    firstSets.at(nonTermIndex).push_back(firstSetSymbol);
                                    reCheck = true;
                                }
                            }
                        }
                        bool inNullset = false;
                        for (int l = 0; l < nullSet.size(); l++)
                        {
                            if (nullSet.at(l) == current)
                            {
                                inNullset = true;
                                break;
                            }
                        }

                        if (!inNullset)
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    /* Task 4:
    *  First, we need to push the $ symbol into the very first follow set. This is a rule of follow sets.
    *  Similarly to first sets, we need to go through all follow sets whenever the follow sets of a non-terminal changes.
    *  We want to check the first symbol of the first rule.
    *  If it's a terminal, we simply go to the next symbol in the rule.
    *  If it's not a terminal, we then must check the symbol after this non-terminal symbol.
    *  If the symbol is at the end of the current vector/rule, we must add the follow set of this nonterminal to the follow set of the current nonterminal we are looking at (barring dupes)
    *  If this symbol is NOT at the end of the current vector/rule, we must look to the ID next to this symbol.
    *  If it is a terminal, we add that terminal to the follow set of the current nonterminal (barring duplicates)
    *  It it is not a terminal, we then need to find the index of the symbol/nonterminal after.
    *  After we find that index, we can then pull up the first set of that nonterminal.
    *  We then add the first set to the follow set of the current nonterminal (barring duplicates).
    *  We then check if that nonterminal that is after the one we are looking at is in the nullSet.
    *  If it is not in the null set, we simply move on. If it is in the null set, we have to look at the next symbol.
    *  Lastly, we need to check if the symbol after the nonterminal we are looking at is at the end.
    *  If so, we then need to find it's index in the LHS array, so that we can add it's followset to the follow set of the current nonterminal.
    *  Finally, if anything changes, we need to go through the entire loop again.
    */

    followSets.at(0).push_back("$");

    reCheck = true;
    while (reCheck)
    {
        reCheck = false;
        for (int i = 0; i < lhs.size(); i++)
        {
            for (int j = 0; j < rhs.at(i).rule.size(); j++)
            {
                for (int k = 0; k < rhs.at(i).rule.at(j).size(); k++)
                {
                    string current = rhs.at(i).rule.at(j).at(k);
                    int currentInd = -100;
                    for (int l = 0; l < nonterm.size(); l++)
                    {
                        if (nonterm.at(l) == current)
                        {
                            currentInd = l;
                            break;
                        }
                    }

                    if (currentInd != -100)
                    {
                        if (k == rhs.at(i).rule.at(j).size() - 1)
                        {
                            int followInd = -1;
                            for (int l = 0; l < nonterm.size(); l++)
                            {
                                if (nonterm.at(l) == lhs.at(i))
                                {
                                    followInd = l;
                                    break;
                                }
                            }

                            for (int l = 0; l < followSets.at(followInd).size(); l++)
                            {
                                bool dupe = false;
                                for (int m = 0; m < followSets.at(currentInd).size(); m++)
                                {
                                    if (followSets.at(currentInd).at(m) == followSets.at(followInd).at(l))
                                    {
                                        dupe = true;
                                        break;
                                    }
                                }

                                if (!dupe)
                                {
                                    followSets.at(currentInd).push_back(followSets.at(followInd).at(l));
                                    reCheck = true;
                                }
                            }
                        }
                        else
                        {
                            for (int n = k + 1; n < rhs.at(i).rule.at(j).size(); n++)
                            {
                                string aftercurrent = rhs.at(i).rule.at(j).at(n);

                                bool isTerminal = true;
                                for (int l = 0; l < nonterm.size(); l++)
                                {
                                    if (nonterm.at(l) == aftercurrent)
                                    {
                                        isTerminal = false;
                                        break;
                                    }
                                }

                                if (isTerminal)
                                {
                                    bool dupe = false;
                                    for (int p = 0; p < followSets.at(currentInd).size(); p++)
                                    {
                                        if (followSets.at(currentInd).at(p) == aftercurrent)
                                        {
                                            dupe = true;
                                            break;
                                        }
                                    }

                                    if (!dupe)
                                    {
                                        followSets.at(currentInd).push_back(aftercurrent);
                                        reCheck = true;
                                    }
                                    break;
                                }
                                else
                                {
                                    int aftercurrentInd = -100;
                                    for (int l = 0; l < nonterm.size(); l++)
                                    {
                                        if (nonterm.at(l) == aftercurrent)
                                        {
                                            aftercurrentInd = l;
                                            break;
                                        }
                                    }

                                    for (int l = 0; l < firstSets.at(aftercurrentInd).size(); l++)
                                    {
                                        string firstSetSymbol = firstSets.at(aftercurrentInd).at(l);

                                        bool dupe = false;
                                        for (int m = 0; m < followSets.at(currentInd).size(); m++)
                                        {
                                            if (followSets.at(currentInd).at(m) == firstSetSymbol)
                                            {
                                                dupe = true;
                                                break;
                                            }
                                        }

                                        if (!dupe)
                                        {
                                            followSets.at(currentInd).push_back(firstSetSymbol);
                                            reCheck = true;
                                        }
                                    }

                                    bool inNullset = false;
                                    for (int l = 0; l < nullSet.size(); l++)
                                    {
                                        if (nullSet.at(l) == aftercurrent)
                                        {
                                            inNullset = true;
                                            break;
                                        }
                                    }

                                    if (!inNullset)
                                    {
                                        break;
                                    }
                                }

                                if (n == rhs.at(i).rule.at(j).size() - 1)
                                {
                                    int lhsInd = -100;
                                    for (int l = 0; l < nonterm.size(); l++)
                                    {
                                        if (nonterm.at(l) == lhs.at(i))
                                        {
                                            lhsInd = l;
                                            break;
                                        }
                                    }

                                    for (int l = 0; l < followSets.at(lhsInd).size(); l++)
                                    {
                                        bool dupe = false;
                                        for (int m = 0; m < followSets.at(currentInd).size(); m++)
                                        {
                                            if (followSets.at(currentInd).at(m) == followSets.at(lhsInd).at(l))
                                            {
                                                dupe = true;
                                                break;
                                            }
                                        }

                                        if (!dupe)
                                        {
                                            followSets.at(currentInd).push_back(followSets.at(lhsInd).at(l));
                                            reCheck = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


void Task1()
{
    for (int i = 0; i < terms.size(); i++)
    {
        cout << terms.at(i) << " ";
    }
    for (int i = 0; i < nonterm.size() - 1; i++)
    {
        cout << nonterm.at(i) << " ";
    }
    cout << nonterm.at(nonterm.size() - 1);
}


void Task2()
{
    cout << "Nullable = { ";
    bool printformat = true;
    for (int i = 0; i < nonterm.size(); i++)
    {
        for (int j = 0; j < nullSet.size(); j++)
        {
            if (nullSet.at(j) == nonterm.at(i))
            {
                if (printformat)
                {
                    cout << nonterm.at(i);
                    printformat = false;
                }
                else
                {
                    cout << ", " << nonterm.at(i);
                }
            }
        }
    }
    cout << " }";
}

void Task3()
{
    for (int i = 0; i < nonterm.size(); i++)
    {
        cout << "FIRST(" << nonterm.at(i) << ") = { ";
        bool printformat = true;
        for (int j = 0; j < terms.size(); j++)
        {
            for (int k = 0; k < firstSets.at(i).size(); k++)
            {
                if (firstSets.at(i).at(k) == terms.at(j))
                {
                    if (printformat)
                    {
                        cout << terms.at(j);
                        printformat = false;
                    }
                    else
                    {
                        cout << ", " << terms.at(j);
                    }
                }
            }
        }
        cout << " }\n";
    }
}
void Task4()
{
    // Print Dollar is there for formatting if I need to print a dollar for a set.
    for (int i = 0; i < nonterm.size(); i++)
    {
        cout << "FOLLOW(" << nonterm.at(i) << ") = { ";
        bool printDollar = true;
        for (int j = 0; j < followSets.at(i).size(); j++)
        {
            if (followSets.at(i).at(j) == "$")
            {
                if (printDollar)
                {
                    cout << "$";
                    printDollar = false;
                }
            }
        }

        for (int j = 0; j < terms.size(); j++)
        {
            for (int k = 0; k < followSets.at(i).size(); k++)
            {
                if (followSets.at(i).at(k) == terms.at(j))
                {
                    if (!printDollar)
                    {
                        cout << ", ";
                    }
                    cout << terms.at(j);
                    printDollar = false;
                }
            }
        }
        cout << " }\n";
    }
}

void Task5()
{
    // Prepare containers for left-factored rules
    vector<string> newLHS;
    vector<Rules> newRHS;

    // Track new non-terminal counters
    map<string, int> newNonTermCounter;

    // Process each non-terminal in the original grammar
    for (int i = 0; i < lhs.size(); i++)
    {
        string currentNT = lhs.at(i);
        Rules currentRules = rhs.at(i);
        vector<vector<string>> remainingRules = currentRules.rule;

        while (true)
        {
            bool foundCommonPrefix = false;

            // Find longest common prefix between rules
            for (int j = 0; j < remainingRules.size(); j++)
            {
                for (int k = j + 1; k < remainingRules.size(); k++)
                {
                    // Determine length of common prefix
                    int prefixLen = 0;
                    while (prefixLen < remainingRules[j].size() && 
                           prefixLen < remainingRules[k].size() && 
                           remainingRules[j][prefixLen] == remainingRules[k][prefixLen])
                    {
                        prefixLen++;
                    }

                    if (prefixLen > 0)
                    {
                        // Create new non-terminal 
                        string newNT = currentNT + to_string(newNonTermCounter[currentNT] + 1);
                        newNonTermCounter[currentNT]++;

                        // Prepare new rule sets
                        vector<vector<string>> newRules;
                        vector<vector<string>> nonPrefixRules;

                        // Separate rules based on common prefix
                        for (int l = 0; l < remainingRules.size(); l++)
                        {
                            if (remainingRules[l].size() >= prefixLen &&
                                equal(remainingRules[l].begin(), 
                                      remainingRules[l].begin() + prefixLen, 
                                      remainingRules[j].begin()))
                            {
                                // Rule with prefix
                                vector<string> remainingRule(
                                    remainingRules[l].begin() + prefixLen, 
                                    remainingRules[l].end()
                                );
                                
                                // Handle epsilon case
                                if (remainingRule.empty())
                                    remainingRule.push_back("#");
                                
                                newRules.push_back(remainingRule);
                            }
                            else
                            {
                                nonPrefixRules.push_back(remainingRules[l]);
                            }
                        }

                        // Construct new rule with prefix
                        vector<string> prefixPart(
                            remainingRules[j].begin(), 
                            remainingRules[j].begin() + prefixLen
                        );
                        prefixPart.push_back(newNT);

                        // Reconstruct rules
                        remainingRules.clear();
                        remainingRules.push_back(prefixPart);
                        remainingRules.insert(
                            remainingRules.end(), 
                            nonPrefixRules.begin(), 
                            nonPrefixRules.end()
                        );

                        // Store new non-terminal rules
                        newLHS.push_back(newNT);
                        Rules newRule;
                        newRule.rule = newRules;
                        newRHS.push_back(newRule);

                        foundCommonPrefix = true;
                        break;
                    }
                }

                if (foundCommonPrefix)
                    break;
            }

            if (!foundCommonPrefix)
                break;
        }

        // Store final rules for this non-terminal
        newLHS.push_back(currentNT);
        Rules finalRule;
        finalRule.rule = remainingRules;
        newRHS.push_back(finalRule);
    }

    // Combine and sort the rules
    vector<pair<string, vector<string>>> sortedRules;
    for (int i = 0; i < newLHS.size(); i++)
    {
        for (auto& rule : newRHS[i].rule)
        {
            sortedRules.push_back({newLHS[i], rule});
        }
    }

    // Sort lexicographically
    sort(sortedRules.begin(), sortedRules.end(), 
        [](const pair<string, vector<string>>& a, 
           const pair<string, vector<string>>& b) {
        string aFull = a.first + " -> ";
        for (auto& sym : a.second) aFull += sym + " ";
        string bFull = b.first + " -> ";
        for (auto& sym : b.second) bFull += sym + " ";
        return aFull < bFull;
    });

    // Print sorted rules
    for (auto& rule : sortedRules)
    {
        cout << rule.first << " -> ";
        if (rule.second.empty() || rule.second[0] == "#")
            cout << "#";
        else
        {
            for (size_t i = 0; i < rule.second.size(); i++)
            {
                cout << rule.second[i] << (i < rule.second.size() - 1 ? " " : "");
            }
        }
        cout << " #" << endl;
    }
}

void Task6()
{
    // Prepare containers for new grammar rules
    vector<string> newLHS;
    vector<Rules> newRHS;

    // Sort non-terminals lexicographically
    vector<string> sortedNonTerminals = nonterm;
    sort(sortedNonTerminals.begin(), sortedNonTerminals.end());

    // Process each non-terminal
    for (int i = 0; i < sortedNonTerminals.size(); i++)
    {
        string currentNT = sortedNonTerminals[i];
        vector<vector<string>> rulesForCurrentNT;

        // Find rules for current non-terminal
        for (int j = 0; j < lhs.size(); j++)
        {
            if (lhs[j] == currentNT)
            {
                for (auto& rule : rhs[j].rule)
                {
                    rulesForCurrentNT.push_back(rule);
                }
            }
        }

        // Eliminate indirect left recursion
        for (int j = 0; j < i; j++)
        {
            string prevNT = sortedNonTerminals[j];
            vector<vector<string>> newRulesSet;

            for (auto& currentRule : rulesForCurrentNT)
            {
                if (!currentRule.empty() && currentRule[0] == prevNT)
                {
                    // Find rules for previous non-terminal
                    vector<vector<string>> prevNTRules;
                    for (int k = 0; k < lhs.size(); k++)
                    {
                        if (lhs[k] == prevNT)
                        {
                            for (auto& rule : rhs[k].rule)
                            {
                                prevNTRules.push_back(rule);
                            }
                        }
                    }

                    // Replace current rule
                    for (auto& prevRule : prevNTRules)
                    {
                        vector<string> newRule = prevRule;
                        newRule.insert(newRule.end(),
                            currentRule.begin() + 1,
                            currentRule.end());
                        newRulesSet.push_back(newRule);
                    }
                }
                else
                {
                    newRulesSet.push_back(currentRule);
                }
            }

            rulesForCurrentNT = newRulesSet;
        }

        // Eliminate direct left recursion
        vector<vector<string>> recursiveRules;
        vector<vector<string>> nonRecursiveRules;

        for (auto& rule : rulesForCurrentNT)
        {
            if (!rule.empty() && rule[0] == currentNT)
            {
                recursiveRules.push_back(rule);
            }
            else
            {
                nonRecursiveRules.push_back(rule);
            }
        }

        if (!recursiveRules.empty())
        {
            // Create new non-terminal
            string newNT = currentNT + "1";
            vector<vector<string>> newNonRecursiveRules;
            vector<vector<string>> newRecursiveRules;

            // Generate non-recursive rules
            for (auto& rule : nonRecursiveRules)
            {
                vector<string> modifiedRule = rule;
                modifiedRule.push_back(newNT);
                newNonRecursiveRules.push_back(modifiedRule);
            }

            // Generate recursive rules
            for (auto& rule : recursiveRules)
            {
                vector<string> modifiedRule(rule.begin() + 1, rule.end());
                modifiedRule.push_back(newNT);
                newRecursiveRules.push_back(modifiedRule);
            }

            // Add epsilon rule
            newRecursiveRules.push_back(vector<string>{}); 

            // Store new rules
            Rules newRule1, newRule2;
            newRule1.rule = newNonRecursiveRules;
            newRule2.rule = newRecursiveRules;

            newLHS.push_back(currentNT);
            newRHS.push_back(newRule1);
            newLHS.push_back(newNT);
            newRHS.push_back(newRule2);
        }
        else
        {
            // No recursion, store original rules
            Rules newRule;
            newRule.rule = rulesForCurrentNT;
            newLHS.push_back(currentNT);
            newRHS.push_back(newRule);
        }
    }

    // Combine and sort the rules
    vector<pair<string, vector<string>>> sortedRules;
    for (int i = 0; i < newLHS.size(); i++)
    {
        for (auto& rule : newRHS[i].rule)
        {
            sortedRules.push_back({newLHS[i], rule});
        }
    }

    // Sort lexicographically
    sort(sortedRules.begin(), sortedRules.end(), 
        [](const pair<string, vector<string>>& a, 
           const pair<string, vector<string>>& b) {
        string aFull = a.first + " -> ";
        for (auto& sym : a.second) aFull += sym + " ";
        string bFull = b.first + " -> ";
        for (auto& sym : b.second) bFull += sym + " ";
        return aFull < bFull;
    });

    // Print sorted rules
    for (auto& rule : sortedRules)
    {
        cout << rule.first << " -> ";
        if (rule.second.empty() || rule.second[0] == "#")
            cout << "#";
        else
        {
            for (size_t i = 0; i < rule.second.size(); i++)
            {
                cout << rule.second[i] << (i < rule.second.size() - 1 ? " " : "");
            }
        }
        cout << " #" << endl;
    }
}
    
int main (int argc, char* argv[])
{
    int task;

    if (argc < 2)
    {
        cout << "Error: missing argument\n";
        return 1;
    }

    /*
       Note that by convention argv[0] is the name of your executable,
       and the first argument to your program is stored in argv[1]
     */
    task = atoi(argv[1]);

    Parser parser;
    parser.ReadGrammar();
    

    switch (task) {
        case 1: Task1();
            break;

        case 2: Task2();
            break;

        case 3: Task3();
            break;

        case 4: Task4();
            break;

        case 5: Task5();
            break;
        
        case 6: Task6();
            break;

        default:
            cout << "Error: unrecognized task number " << task << "\n";
            break;
    }
    return 0;
}
