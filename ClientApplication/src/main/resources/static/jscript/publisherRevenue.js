function getRevenues() {
    fetch(  url+ 'publisher/'+getUserName()+ '/revenue')
        .then(res => res.json())
        .then((data) => {
            let count =0 ;
            let output = '';
            data["content"].forEach(function (revenue) {
                count ++;
                output += `
                <li class="list-group-item">
                    <!-- Custom content-->
                    <div class="media align-items-lg-center flex-column flex-lg-row p-3">
                        <div class="media-body order-2 order-lg-1">
                            <h5 class="mt-0 font-weight-bold mb-2" id="revenue_title `+count + `">Revenue #${revenue["id"]}</h5>

                            <ul class="list-group list-group-flush">
                            <li class="list-group-item" id="revenue_amount `+count + `">Amount: ${revenue["amount"]} €</li>
                            <li class="list-group-item" id="revenue_order `+count + `">Order ID: ${revenue.orderId} </li>
                            <li class="list-group-item" id="revenue_isbn `+count + `">ISBN : ${revenue.isbn} </li>

                            </ul>
                        </div>
                    </div>
                 </li>

                    `;

            });
            document.getElementById('output').innerHTML = output;
        })
        .catch((error) => {
            console.log(error)
        })
}
function getRevenuesTotal() {
    fetch(url + 'publisher/'+ getUserName() +' /revenue/total')
        .then(res => res.json())
        .then((data) => {
            document.getElementById('finalPrice').innerHTML = '<strong>Total: </strong> '+  data +'€';
        })
        .catch((error) => {
            console.log(error)
        })
}

