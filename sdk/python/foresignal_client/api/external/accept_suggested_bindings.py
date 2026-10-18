from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.external_binding import ExternalBinding
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/sources/{source_id}/bindings/accept-suggested".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[ExternalBinding] | None:
    if response.status_code == 201:
        response_201 = []
        _response_201 = response.json()
        for response_201_item_data in _response_201:
            response_201_item = ExternalBinding.from_dict(response_201_item_data)

            response_201.append(response_201_item)

        return response_201

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
) -> Response[Problem | list[ExternalBinding]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[ExternalBinding]]:
    """Accept suggested bindings

     Creates the bindings the manifest suggests (each remains editable). Suggestions that already exist
    are skipped; a suggestion that does not validate (for example a target that needs configuration the
    provider cannot know) is created disabled in state `error` with the problems in `lastError`, for the
    operator to complete.

    **Scope:** `admin`.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[ExternalBinding]]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | list[ExternalBinding] | None:
    """Accept suggested bindings

     Creates the bindings the manifest suggests (each remains editable). Suggestions that already exist
    are skipped; a suggestion that does not validate (for example a target that needs configuration the
    provider cannot know) is created disabled in state `error` with the problems in `lastError`, for the
    operator to complete.

    **Scope:** `admin`.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[ExternalBinding]
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[ExternalBinding]]:
    """Accept suggested bindings

     Creates the bindings the manifest suggests (each remains editable). Suggestions that already exist
    are skipped; a suggestion that does not validate (for example a target that needs configuration the
    provider cannot know) is created disabled in state `error` with the problems in `lastError`, for the
    operator to complete.

    **Scope:** `admin`.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[ExternalBinding]]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | list[ExternalBinding] | None:
    """Accept suggested bindings

     Creates the bindings the manifest suggests (each remains editable). Suggestions that already exist
    are skipped; a suggestion that does not validate (for example a target that needs configuration the
    provider cannot know) is created disabled in state `error` with the problems in `lastError`, for the
    operator to complete.

    **Scope:** `admin`.

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[ExternalBinding]
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
        )
    ).parsed
