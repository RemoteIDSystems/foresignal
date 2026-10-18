from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ask_action import AskAction
from ...models.ask_citation import AskCitation
from ...models.ask_delta_event import AskDeltaEvent
from ...models.ask_progress_event import AskProgressEvent
from ...models.ask_request import AskRequest
from ...models.ask_response import AskResponse
from ...models.ask_stream_licence_policy_item import AskStreamLicencePolicyItem
from ...models.ask_warning_event import AskWarningEvent
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: AskRequest,
    licence_policy: list[AskStreamLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ask/stream",
        "params": params,
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> (
    AskAction
    | AskCitation
    | AskDeltaEvent
    | AskProgressEvent
    | AskResponse
    | AskWarningEvent
    | Problem
    | None
):
    if response.status_code == 200:

        def _parse_response_200(
            data: object,
        ) -> (
            AskAction
            | AskCitation
            | AskDeltaEvent
            | AskProgressEvent
            | AskResponse
            | AskWarningEvent
            | Problem
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = AskProgressEvent.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_1 = AskDeltaEvent.from_dict(data)

                return response_200_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_2 = AskCitation.from_dict(data)

                return response_200_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_3 = AskWarningEvent.from_dict(data)

                return response_200_type_3
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_4 = AskAction.from_dict(data)

                return response_200_type_4
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_5 = AskResponse.from_dict(data)

                return response_200_type_5
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_6 = Problem.from_dict(data)

            return response_200_type_6

        response_200 = _parse_response_200(response.text)

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[
    AskAction
    | AskCitation
    | AskDeltaEvent
    | AskProgressEvent
    | AskResponse
    | AskWarningEvent
    | Problem
]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: AskRequest,
    licence_policy: list[AskStreamLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[
    AskAction
    | AskCitation
    | AskDeltaEvent
    | AskProgressEvent
    | AskResponse
    | AskWarningEvent
    | Problem
]:
    """Ask a question (streaming)

     Server-Sent Events: `progress` (tool activity), `delta` (unverified answer text), `citation`,
    `warning`, `action`, `done` (the full verified AskResponse, identical to the persisted message),
    `error`. Heartbeats and per-token stream limits as in 015.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        licence_policy (list[AskStreamLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskAction | AskCitation | AskDeltaEvent | AskProgressEvent | AskResponse | AskWarningEvent | Problem | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: AskRequest,
    licence_policy: list[AskStreamLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> (
    AskAction
    | AskCitation
    | AskDeltaEvent
    | AskProgressEvent
    | AskResponse
    | AskWarningEvent
    | Problem
    | None
):
    """Ask a question (streaming)

     Server-Sent Events: `progress` (tool activity), `delta` (unverified answer text), `citation`,
    `warning`, `action`, `done` (the full verified AskResponse, identical to the persisted message),
    `error`. Heartbeats and per-token stream limits as in 015.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        licence_policy (list[AskStreamLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskAction | AskCitation | AskDeltaEvent | AskProgressEvent | AskResponse | AskWarningEvent | Problem | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: AskRequest,
    licence_policy: list[AskStreamLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[
    AskAction
    | AskCitation
    | AskDeltaEvent
    | AskProgressEvent
    | AskResponse
    | AskWarningEvent
    | Problem
]:
    """Ask a question (streaming)

     Server-Sent Events: `progress` (tool activity), `delta` (unverified answer text), `citation`,
    `warning`, `action`, `done` (the full verified AskResponse, identical to the persisted message),
    `error`. Heartbeats and per-token stream limits as in 015.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        licence_policy (list[AskStreamLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskAction | AskCitation | AskDeltaEvent | AskProgressEvent | AskResponse | AskWarningEvent | Problem | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        licence_policy=licence_policy,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: AskRequest,
    licence_policy: list[AskStreamLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> (
    AskAction
    | AskCitation
    | AskDeltaEvent
    | AskProgressEvent
    | AskResponse
    | AskWarningEvent
    | Problem
    | None
):
    """Ask a question (streaming)

     Server-Sent Events: `progress` (tool activity), `delta` (unverified answer text), `citation`,
    `warning`, `action`, `done` (the full verified AskResponse, identical to the persisted message),
    `error`. Heartbeats and per-token stream limits as in 015.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        licence_policy (list[AskStreamLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskAction | AskCitation | AskDeltaEvent | AskProgressEvent | AskResponse | AskWarningEvent | Problem | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            licence_policy=licence_policy,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
