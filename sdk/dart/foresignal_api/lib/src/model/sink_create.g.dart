// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkCreateCWProxy {
  SinkCreate id(String id);

  SinkCreate type(SinkCreateTypeEnum type);

  SinkCreate name(String name);

  SinkCreate enabled(bool? enabled);

  SinkCreate config(Map<String, Object> config);

  SinkCreate secrets(Map<String, String>? secrets);

  SinkCreate filter(SinkFilter? filter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkCreate call({
    String id,
    SinkCreateTypeEnum type,
    String name,
    bool? enabled,
    Map<String, Object> config,
    Map<String, String>? secrets,
    SinkFilter? filter,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkCreate.copyWith(...)` or call `instanceOfSinkCreate.copyWith.fieldName(value)` for a single field.
class _$SinkCreateCWProxyImpl implements _$SinkCreateCWProxy {
  const _$SinkCreateCWProxyImpl(this._value);

  final SinkCreate _value;

  @override
  SinkCreate id(String id) => call(id: id);

  @override
  SinkCreate type(SinkCreateTypeEnum type) => call(type: type);

  @override
  SinkCreate name(String name) => call(name: name);

  @override
  SinkCreate enabled(bool? enabled) => call(enabled: enabled);

  @override
  SinkCreate config(Map<String, Object> config) => call(config: config);

  @override
  SinkCreate secrets(Map<String, String>? secrets) => call(secrets: secrets);

  @override
  SinkCreate filter(SinkFilter? filter) => call(filter: filter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkCreate call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
  }) {
    return SinkCreate(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SinkCreateTypeEnum,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      config: config == const $CopyWithPlaceholder() || config == null
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as Map<String, String>?,
      filter: filter == const $CopyWithPlaceholder()
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as SinkFilter?,
    );
  }
}

extension $SinkCreateCopyWith on SinkCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkCreate.copyWith(...)` or `instanceOfSinkCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkCreateCWProxy get copyWith => _$SinkCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkCreate _$SinkCreateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SinkCreate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'type', 'name', 'config']);
  final val = SinkCreate(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$SinkCreateTypeEnumEnumMap, v),
    ),
    name: $checkedConvert('name', (v) => v as String),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    secrets: $checkedConvert(
      'secrets',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    filter: $checkedConvert(
      'filter',
      (v) => v == null ? null : SinkFilter.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SinkCreateToJson(SinkCreate instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': _$SinkCreateTypeEnumEnumMap[instance.type]!,
      'name': instance.name,
      'enabled': ?instance.enabled,
      'config': instance.config,
      'secrets': ?instance.secrets,
      'filter': ?instance.filter?.toJson(),
    };

const _$SinkCreateTypeEnumEnumMap = {
  SinkCreateTypeEnum.mqttState: 'mqtt-state',
  SinkCreateTypeEnum.cot: 'cot',
  SinkCreateTypeEnum.readsbJson: 'readsb-json',
  SinkCreateTypeEnum.sbs: 'sbs',
  SinkCreateTypeEnum.nmeaAivdm: 'nmea-aivdm',
  SinkCreateTypeEnum.aprsIsServe: 'aprs-is-serve',
  SinkCreateTypeEnum.cloudevents: 'cloudevents',
  SinkCreateTypeEnum.federationPush: 'federation-push',
};
