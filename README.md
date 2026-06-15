# ULP

Programming language & transpiler project for UTU course : 
Programming language and compilers *(Spring '26)*

### Language Design

The grammar, in BNF form can be found under docs/grammar.
Sample programs can be found under src/Resources/ .
ULP, (un-natural language programming language), tries to approach the language design with natural language leaning keywords and easy to reason about explicitly written programs. 
Goal was to create a somewhat easier to reason about programs which are harder to type and very explicit. This goal was somewhat achieved for smaller programs,
albeit the need of keywords and somewhat unconventionally expressed parsing ideas lead to a very different and therefore error-prone way of writing programs.

To combat this, last minute changes were made to drop some multi keyword driven statements and expressions into optional status, leading to some simplified statements and expressions,
such as dropping the "number" token from variable initializations, simplifying if-else from check is condition, simplified loops from repeat until to
just until. Subprograms have been also under continuous monitoring state.

I am not exactly proud of every bit on the language, but it does work, at least to an extent, lots of simple test programs were made. Due to the
somewhat limited priorization and time management, the final push for a transpiler in java is in a distant, somewhat impropable roadmap.


### Implementation

ULP is an interpreted scripting style language with static typing, lexical scoping, and supports minimal objects for ease of code reuse.
The package provided is a simple java implementation, mostly in tune with the book Crafing Interpreters (Nystrom 2015). The interpreter does not lend
itself well to testing big programs or complex software. In its current most basic form the language does not even support reading code from multiple
files ie importing modules, but is on the same roadmap.






#### Status : Ongoing