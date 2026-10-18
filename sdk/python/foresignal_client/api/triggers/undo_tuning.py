from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.tuning_log_entry import TuningLogEntry
from ...types import Response


def _get_kwargs(
    rule_id: UUID,
    log_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/triggers/{rule_id}/tuning-log/{log_id}/undo".format(
            rule_id=quote(str(rule_id), safe=""),
            log_id=quote(str(log_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TuningLogEntry | None:
    if response.status_code == 200:
        response_200 = TuningLogEntry.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | TuningLogEntry]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    rule_id: UUID,
    log_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | TuningLogEntry]:
    """Undo an applied tuning change

     Restores the rule's `before` definition through updateTrigger when the rule is unchanged since the
    change was applied; otherwise 409.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        log_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TuningLogEntry]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        log_id=log_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    rule_id: UUID,
    log_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | TuningLogEntry | None:
    """Undo an applied tuning change

     Restores the rule's `before` definition through updateTrigger when the rule is unchanged since the
    change was applied; otherwise 409.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        log_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TuningLogEntry
    """

    return sync_detailed(
        rule_id=rule_id,
        log_id=log_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    log_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | TuningLogEntry]:
    """Undo an applied tuning change

     Restores the rule's `before` definition through updateTrigger when the rule is unchanged since the
    change was applied; otherwise 409.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        log_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TuningLogEntry]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        log_id=log_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    log_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | TuningLogEntry | None:
    """Undo an applied tuning change

     Restores the rule's `before` definition through updateTrigger when the rule is unchanged since the
    change was applied; otherwise 409.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        log_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TuningLogEntry
    """

    return (
        await asyncio_detailed(
            rule_id=rule_id,
            log_id=log_id,
            client=client,
        )
    ).parsed
