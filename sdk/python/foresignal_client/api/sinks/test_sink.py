from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.sink_test_request import SinkTestRequest
from ...models.sink_test_result import SinkTestResult
from ...types import UNSET, Response, Unset


def _get_kwargs(
    sink_id: str,
    *,
    body: SinkTestRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sinks/{sink_id}/test".format(
            sink_id=quote(str(sink_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SinkTestResult | None:
    if response.status_code == 200:
        response_200 = SinkTestResult.from_dict(response.json())

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
) -> Response[Problem | SinkTestResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkTestRequest | Unset = UNSET,
) -> Response[Problem | SinkTestResult]:
    """Test a sink once

     Validates the config, connects (PUSH) or binds (SERVE) once, sends one synthetic record and reports
    the result. The cursor is not touched.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkTestRequest | Unset): Options for a one-off sink test (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SinkTestResult]
    """

    kwargs = _get_kwargs(
        sink_id=sink_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkTestRequest | Unset = UNSET,
) -> Problem | SinkTestResult | None:
    """Test a sink once

     Validates the config, connects (PUSH) or binds (SERVE) once, sends one synthetic record and reports
    the result. The cursor is not touched.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkTestRequest | Unset): Options for a one-off sink test (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SinkTestResult
    """

    return sync_detailed(
        sink_id=sink_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkTestRequest | Unset = UNSET,
) -> Response[Problem | SinkTestResult]:
    """Test a sink once

     Validates the config, connects (PUSH) or binds (SERVE) once, sends one synthetic record and reports
    the result. The cursor is not touched.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkTestRequest | Unset): Options for a one-off sink test (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SinkTestResult]
    """

    kwargs = _get_kwargs(
        sink_id=sink_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    sink_id: str,
    *,
    client: AuthenticatedClient,
    body: SinkTestRequest | Unset = UNSET,
) -> Problem | SinkTestResult | None:
    """Test a sink once

     Validates the config, connects (PUSH) or binds (SERVE) once, sends one synthetic record and reports
    the result. The cursor is not touched.

    **Scope:** `admin`.

    Args:
        sink_id (str):
        body (SinkTestRequest | Unset): Options for a one-off sink test (110)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SinkTestResult
    """

    return (
        await asyncio_detailed(
            sink_id=sink_id,
            client=client,
            body=body,
        )
    ).parsed
