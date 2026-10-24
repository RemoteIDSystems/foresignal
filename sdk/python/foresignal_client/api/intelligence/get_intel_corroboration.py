from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.intel_corroboration import IntelCorroboration
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/intel/corroboration",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IntelCorroboration | Problem | None:
    if response.status_code == 200:
        response_200 = IntelCorroboration.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[IntelCorroboration | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[IntelCorroboration | Problem]:
    """Corroboration policy

     The tunable "promote to a trusted signal" gates (154): the event evidence-quality thresholds (with
    an optional cross-modality requirement), the situation activation rule, and the automatic
    corroboration of lone events. Every gate defaults to its historical constant.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelCorroboration | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> IntelCorroboration | Problem | None:
    """Corroboration policy

     The tunable "promote to a trusted signal" gates (154): the event evidence-quality thresholds (with
    an optional cross-modality requirement), the situation activation rule, and the automatic
    corroboration of lone events. Every gate defaults to its historical constant.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelCorroboration | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[IntelCorroboration | Problem]:
    """Corroboration policy

     The tunable "promote to a trusted signal" gates (154): the event evidence-quality thresholds (with
    an optional cross-modality requirement), the situation activation rule, and the automatic
    corroboration of lone events. Every gate defaults to its historical constant.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelCorroboration | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> IntelCorroboration | Problem | None:
    """Corroboration policy

     The tunable "promote to a trusted signal" gates (154): the event evidence-quality thresholds (with
    an optional cross-modality requirement), the situation activation rule, and the automatic
    corroboration of lone events. Every gate defaults to its historical constant.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelCorroboration | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
