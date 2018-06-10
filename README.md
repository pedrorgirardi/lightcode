# Light Code

Smart completions, code navigation and refactoring for Clojure.

Light Code is about adding 'smartness' to your code, but more is required if we want to have an enjoyable editing experience for Clojure & ClojureScript.

I'm thinking about two kind of experiences:
- Code editing;
- Live programming; 


I should extract my theme customizations into a proper Theme Extension. Light Code itself shouldn't do anything about theme customization.


This is what Light Code should **NOT DO**:
- Theme customizations.
- Format your code. This should be done by a formatter extension like `cljfmt`.
- Structural editing. This should also be done by extensions like `Calva Paredit` or `Parinfer`.

This is what Light Code should **DO**:
- Provide a LanguageConfiguration and `editor.wordSeparators`

  >The language configuration interfaces defines the contract between extensions and various editor features, like automatic bracket insertion, automatic indentation etc.

- Provide a DefinitionProvider

  >The definition provider interface defines the contract between extensions and the go to definition and peek definition features.

- Provide a DocumentSymbolProvider

  >The document symbol provider interface defines the contract between extensions and the go to symbol-feature.

- Provide a ReferenceProvider

  >The reference provider interface defines the contract between extensions and the find references-feature.

- Provide a HoverProvider

  >The hover provider interface defines the contract between extensions and the hover-feature.

- Provide a SignatureHelpProvider

  >The signature help provider interface defines the contract between extensions and the parameter hints-feature.

- Provide a RenameProvider

  >The rename provider interface defines the contract between extensions and the rename-feature.

- Provide a WorkspaceSymbolProvider

  >The workspace symbol provider interface defines the contract between extensions and the symbol search-feature.


TODO

  1. LanguageConfiguration and `editor.wordSeparators` ✅
  1. DefinitionProvider
  1. DocumentSymbolProvider ✅
  1. ReferenceProvider
  1. HoverProvider ✅
  1. SignatureHelpProvider
  1. RenameProvider
  1. WorkspaceSymbolProvider
