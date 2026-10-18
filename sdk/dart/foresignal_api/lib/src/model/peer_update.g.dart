// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerUpdateCWProxy {
  PeerUpdate name(String name);

  PeerUpdate baseUrl(String baseUrl);

  PeerUpdate enabled(bool? enabled);

  PeerUpdate token(String? token);

  PeerUpdate selection(PeerSelection? selection);

  PeerUpdate sameOperator(bool? sameOperator);

  PeerUpdate reliability(PeerUpdateReliabilityEnum? reliability);

  PeerUpdate pinnedKid(String? pinnedKid);

  PeerUpdate exchangeKey(String? exchangeKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  PeerUpdate call({
    String name,
    String baseUrl,
    bool? enabled,
    String? token,
    PeerSelection? selection,
    bool? sameOperator,
    PeerUpdateReliabilityEnum? reliability,
    String? pinnedKid,
    String? exchangeKey,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeerUpdate.copyWith(...)` or call `instanceOfPeerUpdate.copyWith.fieldName(value)` for a single field.
class _$PeerUpdateCWProxyImpl implements _$PeerUpdateCWProxy {
  const _$PeerUpdateCWProxyImpl(this._value);

  final PeerUpdate _value;

  @override
  PeerUpdate name(String name) => call(name: name);

  @override
  PeerUpdate baseUrl(String baseUrl) => call(baseUrl: baseUrl);

  @override
  PeerUpdate enabled(bool? enabled) => call(enabled: enabled);

  @override
  PeerUpdate token(String? token) => call(token: token);

  @override
  PeerUpdate selection(PeerSelection? selection) => call(selection: selection);

  @override
  PeerUpdate sameOperator(bool? sameOperator) =>
      call(sameOperator: sameOperator);

  @override
  PeerUpdate reliability(PeerUpdateReliabilityEnum? reliability) =>
      call(reliability: reliability);

  @override
  PeerUpdate pinnedKid(String? pinnedKid) => call(pinnedKid: pinnedKid);

  @override
  PeerUpdate exchangeKey(String? exchangeKey) => call(exchangeKey: exchangeKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PeerUpdate call({
    Object? name = const $CopyWithPlaceholder(),
    Object? baseUrl = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
    Object? selection = const $CopyWithPlaceholder(),
    Object? sameOperator = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
    Object? pinnedKid = const $CopyWithPlaceholder(),
    Object? exchangeKey = const $CopyWithPlaceholder(),
  }) {
    return PeerUpdate(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      baseUrl: baseUrl == const $CopyWithPlaceholder() || baseUrl == null
          ? _value.baseUrl
          // ignore: cast_nullable_to_non_nullable
          : baseUrl as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      token: token == const $CopyWithPlaceholder()
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String?,
      selection: selection == const $CopyWithPlaceholder()
          ? _value.selection
          // ignore: cast_nullable_to_non_nullable
          : selection as PeerSelection?,
      sameOperator: sameOperator == const $CopyWithPlaceholder()
          ? _value.sameOperator
          // ignore: cast_nullable_to_non_nullable
          : sameOperator as bool?,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as PeerUpdateReliabilityEnum?,
      pinnedKid: pinnedKid == const $CopyWithPlaceholder()
          ? _value.pinnedKid
          // ignore: cast_nullable_to_non_nullable
          : pinnedKid as String?,
      exchangeKey: exchangeKey == const $CopyWithPlaceholder()
          ? _value.exchangeKey
          // ignore: cast_nullable_to_non_nullable
          : exchangeKey as String?,
    );
  }
}

extension $PeerUpdateCopyWith on PeerUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeerUpdate.copyWith(...)` or `instanceOfPeerUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerUpdateCWProxy get copyWith => _$PeerUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PeerUpdate _$PeerUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PeerUpdate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'baseUrl']);
      final val = PeerUpdate(
        name: $checkedConvert('name', (v) => v as String),
        baseUrl: $checkedConvert('baseUrl', (v) => v as String),
        enabled: $checkedConvert('enabled', (v) => v as bool?),
        token: $checkedConvert('token', (v) => v as String?),
        selection: $checkedConvert(
          'selection',
          (v) => v == null
              ? null
              : PeerSelection.fromJson(v as Map<String, dynamic>),
        ),
        sameOperator: $checkedConvert('sameOperator', (v) => v as bool?),
        reliability: $checkedConvert(
          'reliability',
          (v) => $enumDecodeNullable(_$PeerUpdateReliabilityEnumEnumMap, v),
        ),
        pinnedKid: $checkedConvert('pinnedKid', (v) => v as String?),
        exchangeKey: $checkedConvert('exchangeKey', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PeerUpdateToJson(PeerUpdate instance) =>
    <String, dynamic>{
      'name': instance.name,
      'baseUrl': instance.baseUrl,
      'enabled': ?instance.enabled,
      'token': ?instance.token,
      'selection': ?instance.selection?.toJson(),
      'sameOperator': ?instance.sameOperator,
      'reliability': ?_$PeerUpdateReliabilityEnumEnumMap[instance.reliability],
      'pinnedKid': ?instance.pinnedKid,
      'exchangeKey': ?instance.exchangeKey,
    };

const _$PeerUpdateReliabilityEnumEnumMap = {
  PeerUpdateReliabilityEnum.A: 'A',
  PeerUpdateReliabilityEnum.B: 'B',
  PeerUpdateReliabilityEnum.C: 'C',
  PeerUpdateReliabilityEnum.D: 'D',
  PeerUpdateReliabilityEnum.E: 'E',
  PeerUpdateReliabilityEnum.F: 'F',
};
