package main

import (
	"flag"
	"fmt"
	"gitlab.science.ru.nl/rick/fsm"
	"gitlab.science.ru.nl/rick/partition"
	// "time"
)

func main() {
	
	var path = flag.String("path", "", "The path of the dot file.")
	var strategy = flag.Int("strategy", 0, "The algorithm to use for refining the partition: 0 for Hopcroft's (default), 1 for Moore's.")
	flag.Parse()

	// Construct a fsm from the input file.
	m := fsm.DotFile(*path)
	states, inputs, outputs := m.States(), m.Inputs(), m.Outputs()
	// Construct the transition and output function per input.
	fs := make([]func(int) int, 0, inputs)
	gs := make([]func(int) int, 0, inputs)
	for i := 0; i < inputs; i++ {
		f, _ := m.TransitionFunction(i)		
		fs = append(fs, f)
		g, _ := m.OutputFunction(i)
		gs = append(gs, g)
	}
	

/*

	states, inputs, outputs := 6, 3, 2
	transitions := []struct{ from, input, output, to int }{
		{0, 2, 0, 1},
		{0, 2, 0, 0},
		{1, 2, 1, 2},
		{1, 2, 0, 0},
		{2, 2, 0, 3},
		{2, 2, 0, 3},
		{3, 2, 1, 4},
		{3, 2, 0, 4},
		{4, 0, 0, 5},
		{4, 1, 1, 5},
		{5, 0, 1, 0},
		{5, 1, 0, 0},
		{0, 2, 2, 0},
		{1, 2, 2, 0},
		{2, 2, 2, 0},
		{3, 2, 2, 0},
		{4, 2, 2, 0},
		{5, 2, 2, 0},
	}
	m := fsm.New(states, inputs, outputs)

	for _, t := range transitions {
		m.SetTransition(t.from, t.input, t.output, t.to)
	}

	fs := make([]func(int) int, 0, inputs)
	gs := make([]func(int) int, 0, inputs)
	for i := 0; i < inputs; i++ {
		f, _ := m.TransitionFunction(i)		
		fs = append(fs, f)
		g, _ := m.OutputFunction(i)
		gs = append(gs, g)
	}
*/
	// Construct the partition.
	// start := time.Now()


	p := partition.New(states, outputs, gs...)
	p.Refine(*strategy, fs...)


	for i := 0; i < states; i++ {
		for j := 0; j < states; j++ {
			wit := p.Witness(i,j)
			if len(wit) != 0 {
				fmt.Print(wit)
				fmt.Print(";")
			}
		}
	}

}
