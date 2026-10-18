// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerTestResultCWProxy {
  PeerTestResult ok(bool ok);

  PeerTestResult reachable(bool reachable);

  PeerTestResult latencyMs(int? latencyMs);

  PeerTestResult info(FederationInfo? info);

  PeerTestResult signatureValid(bool? signatureValid);

  PeerTestResult kid(String? kid);

  PeerTestResult pinnedKid(String? pinnedKid);

  PeerTestResult keyMatches(bool? keyMatches);

  PeerTestResult error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  PeerTestResult call({
    bool ok,
    bool reachable,
    int? latencyMs,
    FederationInfo? info,
    bool? signatureValid,
    String? kid,
    String? pinnedKid,
    bool? keyMatches,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeerTestResult.copyWith(...)` or call `instanceOfPeerTestResult.copyWith.fieldName(value)` for a single field.
class _$PeerTestResultCWProxyImpl implements _$PeerTestResultCWProxy {
  const _$PeerTestResultCWProxyImpl(this._value);

  final PeerTestResult _value;

  @override
  PeerTestResult ok(bool ok) => call(ok: ok);

  @override
  PeerTestResult reachable(bool reachable) => call(reachable: reachable);

  @override
  PeerTestResult latencyMs(int? latencyMs) => call(latencyMs: latencyMs);

  @override
  PeerTestResult info(FederationInfo? info) => call(info: info);

  @override
  PeerTestResult signatureValid(bool? signatureValid) =>
      call(signatureValid: signatureValid);

  @override
  PeerTestResult kid(String? kid) => call(kid: kid);

  @override
  PeerTestResult pinnedKid(String? pinnedKid) => call(pinnedKid: pinnedKid);

  @override
  PeerTestResult keyMatches(bool? keyMatches) => call(keyMatches: keyMatches);

  @override
  PeerTestResult error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PeerTestResult call({
    Object? ok = const $CopyWithPlaceholder(),
    Object? reachable = const $CopyWithPlaceholder(),
    Object? latencyMs = const $CopyWithPlaceholder(),
    Object? info = const $CopyWithPlaceholder(),
    Object? signatureValid = const $CopyWithPlaceholder(),
    Object? kid = const $CopyWithPlaceholder(),
    Object? pinnedKid = const $CopyWithPlaceholder(),
    Object? keyMatches = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return PeerTestResult(
      ok: ok == const $CopyWithPlaceholder() || ok == null
          ? _value.ok
          // ignore: cast_nullable_to_non_nullable
          : ok as bool,
      reachable: reachable == const $CopyWithPlaceholder() || reachable == null
          ? _value.reachable
          // ignore: cast_nullable_to_non_nullable
          : reachable as bool,
      latencyMs: latencyMs == const $CopyWithPlaceholder()
          ? _value.latencyMs
          // ignore: cast_nullable_to_non_nullable
          : latencyMs as int?,
      info: info == const $CopyWithPlaceholder()
          ? _value.info
          // ignore: cast_nullable_to_non_nullable
          : info as FederationInfo?,
      signatureValid: signatureValid == const $CopyWithPlaceholder()
          ? _value.signatureValid
          // ignore: cast_nullable_to_non_nullable
          : signatureValid as bool?,
      kid: kid == const $CopyWithPlaceholder()
          ? _value.kid
          // ignore: cast_nullable_to_non_nullable
          : kid as String?,
      pinnedKid: pinnedKid == const $CopyWithPlaceholder()
          ? _value.pinnedKid
          // ignore: cast_nullable_to_non_nullable
          : pinnedKid as String?,
      keyMatches: keyMatches == const $CopyWithPlaceholder()
          ? _value.keyMatches
          // ignore: cast_nullable_to_non_nullable
          : keyMatches as bool?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $PeerTestResultCopyWith on PeerTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeerTestResult.copyWith(...)` or `instanceOfPeerTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerTestResultCWProxy get copyWith => _$PeerTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PeerTestResult _$PeerTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PeerTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ok', 'reachable']);
      final val = PeerTestResult(
        ok: $checkedConvert('ok', (v) => v as bool),
        reachable: $checkedConvert('reachable', (v) => v as bool),
        latencyMs: $checkedConvert('latencyMs', (v) => (v as num?)?.toInt()),
        info: $checkedConvert(
          'info',
          (v) => v == null
              ? null
              : FederationInfo.fromJson(v as Map<String, dynamic>),
        ),
        signatureValid: $checkedConvert('signatureValid', (v) => v as bool?),
        kid: $checkedConvert('kid', (v) => v as String?),
        pinnedKid: $checkedConvert('pinnedKid', (v) => v as String?),
        keyMatches: $checkedConvert('keyMatches', (v) => v as bool?),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PeerTestResultToJson(PeerTestResult instance) =>
    <String, dynamic>{
      'ok': instance.ok,
      'reachable': instance.reachable,
      'latencyMs': ?instance.latencyMs,
      'info': ?instance.info?.toJson(),
      'signatureValid': ?instance.signatureValid,
      'kid': ?instance.kid,
      'pinnedKid': ?instance.pinnedKid,
      'keyMatches': ?instance.keyMatches,
      'error': ?instance.error,
    };
