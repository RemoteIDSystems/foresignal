from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecaster_comparison import ForecasterComparison
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    a: str,
    b: str,
    category: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["a"] = a

    params["b"] = b

    params["category"] = category

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/compare",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecasterComparison | Problem | None:
    if response.status_code == 200:
        response_200 = ForecasterComparison.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ForecasterComparison | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    category: str | Unset = UNSET,
) -> Response[ForecasterComparison | Problem]:
    """Compare two forecasters

     Compares two forecasters on shared resolved questions only, with a paired bootstrap 95% interval
    (1,000 resamples, deterministic seed).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecasterComparison | Problem]
    """

    kwargs = _get_kwargs(
        a=a,
        b=b,
        category=category,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    category: str | Unset = UNSET,
) -> ForecasterComparison | Problem | None:
    """Compare two forecasters

     Compares two forecasters on shared resolved questions only, with a paired bootstrap 95% interval
    (1,000 resamples, deterministic seed).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecasterComparison | Problem
    """

    return sync_detailed(
        client=client,
        a=a,
        b=b,
        category=category,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    category: str | Unset = UNSET,
) -> Response[ForecasterComparison | Problem]:
    """Compare two forecasters

     Compares two forecasters on shared resolved questions only, with a paired bootstrap 95% interval
    (1,000 resamples, deterministic seed).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecasterComparison | Problem]
    """

    kwargs = _get_kwargs(
        a=a,
        b=b,
        category=category,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    a: str,
    b: str,
    category: str | Unset = UNSET,
) -> ForecasterComparison | Problem | None:
    """Compare two forecasters

     Compares two forecasters on shared resolved questions only, with a paired bootstrap 95% interval
    (1,000 resamples, deterministic seed).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        a (str):
        b (str):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecasterComparison | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            a=a,
            b=b,
            category=category,
        )
    ).parsed
