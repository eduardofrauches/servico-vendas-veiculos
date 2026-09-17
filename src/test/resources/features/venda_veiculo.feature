# language: pt
Funcionalidade: Venda de veiculo
  Como comprador
  Quero comprar um veiculo disponivel
  Para que a venda seja confirmada apos a aprovacao do pagamento

  Cenario: Fluxo completo - cadastro, venda e pagamento aprovado
    Dado que o veiculo de id 501 esta cadastrado e disponivel para venda
    Quando o comprador de CPF "529.982.247-25" efetua a compra do veiculo 501
    E o pagamento da venda e aprovado
    Entao o veiculo 501 deve aparecer na listagem de veiculos vendidos

  Cenario: Pagamento cancelado devolve o veiculo para disponivel
    Dado que o veiculo de id 502 esta cadastrado e disponivel para venda
    Quando o comprador de CPF "390.533.447-05" efetua a compra do veiculo 502
    E o pagamento da venda e cancelado
    Entao o veiculo 502 deve aparecer na listagem de veiculos disponiveis
