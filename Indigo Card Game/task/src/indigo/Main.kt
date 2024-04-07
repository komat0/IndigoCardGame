package indigo

import kotlin.random.Random


fun main() {
    val deck = mutableListOf<String>()
    val table = mutableListOf<String>()
    val humanHand = mutableListOf<String>()
    val aiHand = mutableListOf<String>()

    generateDeck(deck)

    println("Indigo Card Game")
    val humanFirst = whoFirst()
    generateFirstTable(deck, table)

    playGame(deck, table, humanHand, aiHand, humanFirst)
}

val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
val suits = listOf("♦", "♥", "♠", "♣")
var go = false

fun playGame(
    deck: MutableList<String>,
    table: MutableList<String>,
    humanHand: MutableList<String>,
    aiHand: MutableList<String>,
    humanFirst: Boolean,
) {
    val humanScore = mutableListOf<String>()
    val aiScore = mutableListOf<String>()
    var turn = if (humanFirst) "Player" else "Computer"
    var lastWinner = " "
    val playersHandAmount = 6
    var exit = false

    outerCycle@ while (!winCheck(deck, humanHand, aiHand)) {
        if (turn == "Player") {
            if (humanHand.isEmpty()) collector(playersHandAmount, deck, humanHand)
            printTable(table)
            println(
                "Cards in hand: ${
                    humanHand.mapIndexed { index, value -> "${index + 1})$value" }.joinToString(" ")
                }"
            )
            while (true) {
                println("Choose a card to play (1-${humanHand.size}):")
                try {
                    val userInput = readln()
                    if (userInput == "exit") {
                        go = true
                        exit = true
                        break@outerCycle
                    }
                    if (userInput.toInt() in 1..humanHand.size) {
                        table.add(humanHand[userInput.toInt() - 1])
                        humanHand.removeAt(userInput.toInt() - 1)
                        break
                    }
                } catch (e: NumberFormatException) {
                    continue
                }
            }
        } else {
            // Ai Turn
            if (aiHand.isEmpty()) collector(playersHandAmount, deck, aiHand)
            printTable(table)
            val candidate = candidateCards(table, aiHand)
            println(
                aiHand.joinToString(" ")
            )
            println("Computer plays $candidate")
            table.add(candidate)
            aiHand.remove(candidate)
        }

        if (matchChecking(table)) {
            if (turn == "Player") humanScore.addAll(table) else aiScore.addAll(table)
            table.clear()
            lastWinner = turn
            println("$turn wins cards")
            scoreCalculator(humanScore, aiScore, lastWinner)
        }
        turn = if (turn == "Player") "Computer" else "Player"
        if (winCheck(deck, humanHand, aiHand)) go = true
    }

    if (exit) {
        println("Game Over")
    } else {
        printTable(table)

        if (lastWinner == "Player") humanScore.addAll(table) else aiScore.addAll(table)
        scoreCalculator(humanScore, aiScore, lastWinner)
        println("Game Over")
    }
}

fun scoreCalculator(
    humanScoreList: MutableList<String>,
    aiScoreList: MutableList<String>,
    lastWinner: String
) {
    val comparisonSymbols = listOf("A", "10", "J", "Q", "K")
    var humanScore = 0
    var aiScore = 0
    for (card in humanScoreList) {
        if (card.dropLast(1) in comparisonSymbols) humanScore++
    }
    for (card in aiScoreList) {
        if (card.dropLast(1) in comparisonSymbols) aiScore++
    }
    if (go) {
        when {
            humanScoreList.size > aiScoreList.size -> humanScore += 3
            aiScoreList.size > humanScoreList.size -> aiScore += 3
            lastWinner == "Player" -> humanScore += 3
            lastWinner == "Computer" -> aiScore += 3
            humanScore == 0 && aiScore == 0 -> {
                if (whoFirst()) humanScore += 3
                else aiScore += 3
            }
        }
    }
    println(
        """Score: Player $humanScore - Computer $aiScore
Cards: Player ${humanScoreList.size} - Computer ${aiScoreList.size}"""
    )
}

fun matchChecking(table: MutableList<String>): Boolean {
    if (table.size != 1) {

        val lastRank = table.last().dropLast(1)
        val penultimateRank = table[table.size - 2].dropLast(1)

        val lastSuit = table.last().last()
        val penultimateSuit = table[table.size - 2].last()

        return (lastRank == penultimateRank || lastSuit == penultimateSuit)
    } else {
        return false
    }
}

fun printTable(table: MutableList<String>) {
    if (table.isEmpty()) println("No cards on the table") else println(
        "${table.size} " + "cards on the table, and the top card is ${table.last()}"
    )
}

fun collector(repeatTimes: Int, collectFrom: MutableList<String>, collectTo: MutableList<String>) {
    repeat(repeatTimes) {
        collectTo.add(collectFrom.first())
        collectFrom.removeAt(0)
    }
}

fun generateDeck(deck: MutableList<String>) {
    deck.clear()
    for (suit in suits) {
        for (rank in ranks) {
            deck.add("$rank$suit")
        }
    }
    deck.shuffle()
}

fun generateFirstTable(deck: MutableList<String>, table: MutableList<String>) {
    val firstTableCardAmount = 4
    collector(firstTableCardAmount, deck, table)
    println("Initial cards on the table: ${table.joinToString(" ")}")
    println()
}

fun whoFirst(): Boolean {
    while (true) {
        println("Play first?")
        val input = readln().lowercase()
        if (input == "yes") return true
        else if (input == "no") return false
    }
}

fun winCheck(deck: List<String>, humanHand: List<String>, aiHand: List<String>): Boolean {
    return deck.isEmpty() && humanHand.isEmpty() && aiHand.isEmpty()
}

fun candidateCards(table: MutableList<String>, aiHand: MutableList<String>): String {
    val candidateList = mutableSetOf<String>()
    val suitsList = mutableListOf<String>()
    val ranksList = mutableListOf<String>()

    if (table.size == 0) {
        zeroMatch(aiHand, suitsList, ranksList)
    }

    if (table.size > 0) {
        for (card in aiHand) {
            if (card.last() == table.last().last()) suitsList.add(card)
            if (card.dropLast(1) == table.last().dropLast(1)) ranksList.add(card)
        }
    }

    if (table.size > 0 && suitsList.isEmpty() && ranksList.isEmpty()) {
        zeroMatch(aiHand, suitsList, ranksList)
    }
    candidateList.addAll(suitsList)
    candidateList.addAll(ranksList)

    return when {
        aiHand.size == 1 -> aiHand.first()
        candidateList.size == 1 -> candidateList.first()
        suitsList.size > ranksList.size -> suitsList.shuffled().first()
        suitsList.size == ranksList.size && suitsList.size > 0 -> suitsList.shuffled().first()
        suitsList.size > 1 -> suitsList.shuffled().first()
        ranksList.size > 1 -> ranksList.shuffled().first()
        else -> aiHand[Random.nextInt(0, aiHand.size)]
    }
}

fun zeroMatch(aiHand: MutableList<String>, suitsList: MutableList<String>, rankList: MutableList<String>) {
    for (sign in suits) {
        if (aiHand.count { it.last().toString() == sign } > 1) {
            for (card in aiHand) {
                if (card.last().toString() == sign) suitsList.add(card)
            }
        }
    }

    for (rank in ranks) {
        if (aiHand.count { it.dropLast(1) == rank } > 1) {
            for (card in aiHand) {
                if (card.dropLast(1) == rank) rankList.add(card)
            }
        }
    }
}