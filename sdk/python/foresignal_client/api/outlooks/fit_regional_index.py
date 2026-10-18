from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.index_fit_result import IndexFitResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    index_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/indices/{index_id}/fit".format(
            index_id=quote(str(index_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IndexFitResult | Problem | None:
    if response.status_code == 202:
        response_202 = IndexFitResult.from_dict(response.json())

        return response_202

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[IndexFitResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    index_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[IndexFitResult | Problem]:
    """Fit index weights to outcomes

     Starts a regularised NB regression of the next-4-week target count on the lagged normalised
    components across regions, with rolling-origin validation. Weights are proposed (SSE
    `index.weights_proposed`), never applied unless `autoApplyFittedWeights` is true.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IndexFitResult | Problem]
    """

    kwargs = _get_kwargs(
        index_id=index_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    index_id: str,
    *,
    client: AuthenticatedClient,
) -> IndexFitResult | Problem | None:
    """Fit index weights to outcomes

     Starts a regularised NB regression of the next-4-week target count on the lagged normalised
    components across regions, with rolling-origin validation. Weights are proposed (SSE
    `index.weights_proposed`), never applied unless `autoApplyFittedWeights` is true.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IndexFitResult | Problem
    """

    return sync_detailed(
        index_id=index_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    index_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[IndexFitResult | Problem]:
    """Fit index weights to outcomes

     Starts a regularised NB regression of the next-4-week target count on the lagged normalised
    components across regions, with rolling-origin validation. Weights are proposed (SSE
    `index.weights_proposed`), never applied unless `autoApplyFittedWeights` is true.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IndexFitResult | Problem]
    """

    kwargs = _get_kwargs(
        index_id=index_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    index_id: str,
    *,
    client: AuthenticatedClient,
) -> IndexFitResult | Problem | None:
    """Fit index weights to outcomes

     Starts a regularised NB regression of the next-4-week target count on the lagged normalised
    components across regions, with rolling-origin validation. Weights are proposed (SSE
    `index.weights_proposed`), never applied unless `autoApplyFittedWeights` is true.

    **Scope:** `agent`.

    Args:
        index_id (str):  Example: instability.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IndexFitResult | Problem
    """

    return (
        await asyncio_detailed(
            index_id=index_id,
            client=client,
        )
    ).parsed
