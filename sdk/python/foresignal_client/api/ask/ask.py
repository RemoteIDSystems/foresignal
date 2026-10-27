from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ask_licence_policy_item import AskLicencePolicyItem
from ...models.ask_request import AskRequest
from ...models.ask_response import AskResponse
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: AskRequest,
    licence_policy: list[AskLicencePolicyItem] | Unset = UNSET,
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
        "url": "/v1/ask",
        "params": params,
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AskResponse | Problem | None:
    if response.status_code == 200:
        response_200 = AskResponse.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AskResponse | Problem]:
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
    licence_policy: list[AskLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[AskResponse | Problem]:
    """Ask a question

     Answers from Foresignal's data with the chat provider and the read tool catalogue (tools mode, ≤ 12
    iterations, 120 s) or, when the model lacks tools, retrieval-only mode. Every citation is verified
    against tool output down to the persisted seq (`[source:externalId@seq]`); dropped citations and
    uncited claims are returned in `warnings`. Write actions are only proposed unless `allowActions` is
    true and the token has agent scope. A model refusal is answered (200) with a warning. Errors: 409
    LLM_NOT_CONFIGURED, 429 LLM_BUDGET_EXCEEDED.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        licence_policy (list[AskLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskResponse | Problem]
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
    licence_policy: list[AskLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> AskResponse | Problem | None:
    """Ask a question

     Answers from Foresignal's data with the chat provider and the read tool catalogue (tools mode, ≤ 12
    iterations, 120 s) or, when the model lacks tools, retrieval-only mode. Every citation is verified
    against tool output down to the persisted seq (`[source:externalId@seq]`); dropped citations and
    uncited claims are returned in `warnings`. Write actions are only proposed unless `allowActions` is
    true and the token has agent scope. A model refusal is answered (200) with a warning. Errors: 409
    LLM_NOT_CONFIGURED, 429 LLM_BUDGET_EXCEEDED.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        licence_policy (list[AskLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskResponse | Problem
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
    licence_policy: list[AskLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[AskResponse | Problem]:
    """Ask a question

     Answers from Foresignal's data with the chat provider and the read tool catalogue (tools mode, ≤ 12
    iterations, 120 s) or, when the model lacks tools, retrieval-only mode. Every citation is verified
    against tool output down to the persisted seq (`[source:externalId@seq]`); dropped citations and
    uncited claims are returned in `warnings`. Write actions are only proposed unless `allowActions` is
    true and the token has agent scope. A model refusal is answered (200) with a warning. Errors: 409
    LLM_NOT_CONFIGURED, 429 LLM_BUDGET_EXCEEDED.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        licence_policy (list[AskLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskResponse | Problem]
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
    licence_policy: list[AskLicencePolicyItem] | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> AskResponse | Problem | None:
    """Ask a question

     Answers from Foresignal's data with the chat provider and the read tool catalogue (tools mode, ≤ 12
    iterations, 120 s) or, when the model lacks tools, retrieval-only mode. Every citation is verified
    against tool output down to the persisted seq (`[source:externalId@seq]`); dropped citations and
    uncited claims are returned in `warnings`. Write actions are only proposed unless `allowActions` is
    true and the token has agent scope. A model refusal is answered (200) with a warning. Errors: 409
    LLM_NOT_CONFIGURED, 429 LLM_BUDGET_EXCEEDED.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        licence_policy (list[AskLicencePolicyItem] | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.
        body (AskRequest):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskResponse | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            licence_policy=licence_policy,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
