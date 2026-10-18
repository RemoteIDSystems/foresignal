from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.pol_subject import PolSubject
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    subject_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/pol/subjects/{subject_id}/recompute".format(
            subject_id=quote(str(subject_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PolSubject | Problem | None:
    if response.status_code == 202:
        response_202 = PolSubject.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[PolSubject | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    subject_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PolSubject | Problem]:
    """Recompute a subject's places

     Runs the nightly clustering now over the trailing lookback of stored stays.

    **Scope:** `agent`.

    Args:
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PolSubject | Problem]
    """

    kwargs = _get_kwargs(
        subject_id=subject_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    subject_id: str,
    *,
    client: AuthenticatedClient,
) -> PolSubject | Problem | None:
    """Recompute a subject's places

     Runs the nightly clustering now over the trailing lookback of stored stays.

    **Scope:** `agent`.

    Args:
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PolSubject | Problem
    """

    return sync_detailed(
        subject_id=subject_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    subject_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PolSubject | Problem]:
    """Recompute a subject's places

     Runs the nightly clustering now over the trailing lookback of stored stays.

    **Scope:** `agent`.

    Args:
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PolSubject | Problem]
    """

    kwargs = _get_kwargs(
        subject_id=subject_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    subject_id: str,
    *,
    client: AuthenticatedClient,
) -> PolSubject | Problem | None:
    """Recompute a subject's places

     Runs the nightly clustering now over the trailing lookback of stored stays.

    **Scope:** `agent`.

    Args:
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PolSubject | Problem
    """

    return (
        await asyncio_detailed(
            subject_id=subject_id,
            client=client,
        )
    ).parsed
