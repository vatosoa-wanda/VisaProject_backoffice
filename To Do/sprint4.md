nouvelle appli vue js appel api du backend spring boot :
- page :
    - input num passeport ou num demande
    - si num demande :
        - la demande correspondant mis en evidence
        - liste des autres demandes reliees au demandeur

    - si num passeport :
        - liste des demandes reliees au demandeur ordre descendant :
            - les derniers inseres sont affiches en premier :
                - avec historique des statuts de la demande (CREE, SCAN TERMINEE, APPROUVEE)

    - bouton ok :
        - resultat, meme que selui du fiche obtenu par le qr code


- QR Code : num demande , num passeport encode 
