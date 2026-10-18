// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'peer_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PeerCreateCWProxy {
  PeerCreate id(String id);

  PeerCreate name(String name);

  PeerCreate baseUrl(String baseUrl);

  PeerCreate mode(PeerCreateModeEnum? mode);

  PeerCreate enabled(bool? enabled);

  PeerCreate token(String? token);

  PeerCreate selection(PeerSelection? selection);

  PeerCreate sameOperator(bool? sameOperator);

  PeerCreate reliability(PeerCreateReliabilityEnum? reliability);

  PeerCreate exchangeKey(String? exchangeKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  PeerCreate call({
    String id,
    String name,
    String baseUrl,
    PeerCreateModeEnum? mode,
    bool? enabled,
    String? token,
    PeerSelection? selection,
    bool? sameOperator,
    PeerCreateReliabilityEnum? reliability,
    String? exchangeKey,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPeerCreate.copyWith(...)` or call `instanceOfPeerCreate.copyWith.fieldName(value)` for a single field.
class _$PeerCreateCWProxyImpl implements _$PeerCreateCWProxy {
  const _$PeerCreateCWProxyImpl(this._value);

  final PeerCreate _value;

  @override
  PeerCreate id(String id) => call(id: id);

  @override
  PeerCreate name(String name) => call(name: name);

  @override
  PeerCreate baseUrl(String baseUrl) => call(baseUrl: baseUrl);

  @override
  PeerCreate mode(PeerCreateModeEnum? mode) => call(mode: mode);

  @override
  PeerCreate enabled(bool? enabled) => call(enabled: enabled);

  @override
  PeerCreate token(String? token) => call(token: token);

  @override
  PeerCreate selection(PeerSelection? selection) => call(selection: selection);

  @override
  PeerCreate sameOperator(bool? sameOperator) =>
      call(sameOperator: sameOperator);

  @override
  PeerCreate reliability(PeerCreateReliabilityEnum? reliability) =>
      call(reliability: reliability);

  @override
  PeerCreate exchangeKey(String? exchangeKey) => call(exchangeKey: exchangeKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PeerCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PeerCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PeerCreate call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? baseUrl = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
    Object? selection = const $CopyWithPlaceholder(),
    Object? sameOperator = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
    Object? exchangeKey = const $CopyWithPlaceholder(),
  }) {
    return PeerCreate(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      baseUrl: baseUrl == const $CopyWithPlaceholder() || baseUrl == null
          ? _value.baseUrl
          // ignore: cast_nullable_to_non_nullable
          : baseUrl as String,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as PeerCreateModeEnum?,
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
          : reliability as PeerCreateReliabilityEnum?,
      exchangeKey: exchangeKey == const $CopyWithPlaceholder()
          ? _value.exchangeKey
          // ignore: cast_nullable_to_non_nullable
          : exchangeKey as String?,
    );
  }
}

extension $PeerCreateCopyWith on PeerCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPeerCreate.copyWith(...)` or `instanceOfPeerCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PeerCreateCWProxy get copyWith => _$PeerCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PeerCreate _$PeerCreateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PeerCreate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'name', 'baseUrl']);
  final val = PeerCreate(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    baseUrl: $checkedConvert('baseUrl', (v) => v as String),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecodeNullable(_$PeerCreateModeEnumEnumMap, v),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool?),
    token: $checkedConvert('token', (v) => v as String?),
    selection: $checkedConvert(
      'selection',
      (v) =>
          v == null ? null : PeerSelection.fromJson(v as Map<String, dynamic>),
    ),
    sameOperator: $checkedConvert('sameOperator', (v) => v as bool? ?? false),
    reliability: $checkedConvert(
      'reliability',
      (v) => $enumDecodeNullable(_$PeerCreateReliabilityEnumEnumMap, v),
    ),
    exchangeKey: $checkedConvert('exchangeKey', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PeerCreateToJson(PeerCreate instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'baseUrl': instance.baseUrl,
      'mode': ?_$PeerCreateModeEnumEnumMap[instance.mode],
      'enabled': ?instance.enabled,
      'token': ?instance.token,
      'selection': ?instance.selection?.toJson(),
      'sameOperator': ?instance.sameOperator,
      'reliability': ?_$PeerCreateReliabilityEnumEnumMap[instance.reliability],
      'exchangeKey': ?instance.exchangeKey,
    };

const _$PeerCreateModeEnumEnumMap = {
  PeerCreateModeEnum.PULL: 'PULL',
  PeerCreateModeEnum.RECEIVE: 'RECEIVE',
};

const _$PeerCreateReliabilityEnumEnumMap = {
  PeerCreateReliabilityEnum.A: 'A',
  PeerCreateReliabilityEnum.B: 'B',
  PeerCreateReliabilityEnum.C: 'C',
  PeerCreateReliabilityEnum.D: 'D',
  PeerCreateReliabilityEnum.E: 'E',
  PeerCreateReliabilityEnum.F: 'F',
};
