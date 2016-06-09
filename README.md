# Casper (in progress...)

A program that checks css files for duplicate declarations of styles on the same element, Id or class. 
Can be used when adding external css libraries to a web project.
  
Examples of duplicates:

    .sky{color: blue;}
    .sky{color: orange;}
    .sky{position: fixed;}

It will also mark weak duplicates (controlled by an option) such as

    .sky{color: blue;}
    .venus .sky{color: red;}
    .mars > .sky {color: purple;}
    #jupiter .sky{position: absolute}
    
It ignores @ rules for now (until a reason for going through the stress of including them is found)
    
    @media, @font-face, etc  

It also ignores the value of the filter attribute like so

    .flint-michigan-water{filter: No support for filter prop;}
