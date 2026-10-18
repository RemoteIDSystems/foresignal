from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.external_binding_preview import ExternalBindingPreview
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    binding_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/bindings/{binding_id}/preview".format(
            binding_id=quote(str(binding_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExternalBindingPreview | Problem | None:
    if response.status_code == 200:
        response_200 = ExternalBindingPreview.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExternalBindingPreview | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    binding_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExternalBindingPreview | Problem]:
    """Preview a binding

     Evaluates the binding over the last 100 items without side effects.

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalBindingPreview | Problem]
    """

    kwargs = _get_kwargs(
        binding_id=binding_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    binding_id: str,
    *,
    client: AuthenticatedClient,
) -> ExternalBindingPreview | Problem | None:
    """Preview a binding

     Evaluates the binding over the last 100 items without side effects.

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalBindingPreview | Problem
    """

    return sync_detailed(
        binding_id=binding_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    binding_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExternalBindingPreview | Problem]:
    """Preview a binding

     Evaluates the binding over the last 100 items without side effects.

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExternalBindingPreview | Problem]
    """

    kwargs = _get_kwargs(
        binding_id=binding_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    binding_id: str,
    *,
    client: AuthenticatedClient,
) -> ExternalBindingPreview | Problem | None:
    """Preview a binding

     Evaluates the binding over the last 100 items without side effects.

    **Scope:** `admin`.

    Args:
        binding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExternalBindingPreview | Problem
    """

    return (
        await asyncio_detailed(
            binding_id=binding_id,
            client=client,
        )
    ).parsed
