// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_type.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkTypeCWProxy {
  SinkType id(SinkTypeIdEnum id);

  SinkType name(String name);

  SinkType description(String? description);

  SinkType kind(SinkTypeKindEnum kind);

  SinkType modes(List<SinkTypeModesEnum> modes);

  SinkType configSchema(Map<String, Object> configSchema);

  SinkType secretFields(List<String> secretFields);

  SinkType defaultMaxPerSecond(int? defaultMaxPerSecond);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkType(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkType(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkType call({
    SinkTypeIdEnum id,
    String name,
    String? description,
    SinkTypeKindEnum kind,
    List<SinkTypeModesEnum> modes,
    Map<String, Object> configSchema,
    List<String> secretFields,
    int? defaultMaxPerSecond,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkType.copyWith(...)` or call `instanceOfSinkType.copyWith.fieldName(value)` for a single field.
class _$SinkTypeCWProxyImpl implements _$SinkTypeCWProxy {
  const _$SinkTypeCWProxyImpl(this._value);

  final SinkType _value;

  @override
  SinkType id(SinkTypeIdEnum id) => call(id: id);

  @override
  SinkType name(String name) => call(name: name);

  @override
  SinkType description(String? description) => call(description: description);

  @override
  SinkType kind(SinkTypeKindEnum kind) => call(kind: kind);

  @override
  SinkType modes(List<SinkTypeModesEnum> modes) => call(modes: modes);

  @override
  SinkType configSchema(Map<String, Object> configSchema) =>
      call(configSchema: configSchema);

  @override
  SinkType secretFields(List<String> secretFields) =>
      call(secretFields: secretFields);

  @override
  SinkType defaultMaxPerSecond(int? defaultMaxPerSecond) =>
      call(defaultMaxPerSecond: defaultMaxPerSecond);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkType(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkType(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkType call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? modes = const $CopyWithPlaceholder(),
    Object? configSchema = const $CopyWithPlaceholder(),
    Object? secretFields = const $CopyWithPlaceholder(),
    Object? defaultMaxPerSecond = const $CopyWithPlaceholder(),
  }) {
    return SinkType(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as SinkTypeIdEnum,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as SinkTypeKindEnum,
      modes: modes == const $CopyWithPlaceholder() || modes == null
          ? _value.modes
          // ignore: cast_nullable_to_non_nullable
          : modes as List<SinkTypeModesEnum>,
      configSchema:
          configSchema == const $CopyWithPlaceholder() || configSchema == null
          ? _value.configSchema
          // ignore: cast_nullable_to_non_nullable
          : configSchema as Map<String, Object>,
      secretFields:
          secretFields == const $CopyWithPlaceholder() || secretFields == null
          ? _value.secretFields
          // ignore: cast_nullable_to_non_nullable
          : secretFields as List<String>,
      defaultMaxPerSecond: defaultMaxPerSecond == const $CopyWithPlaceholder()
          ? _value.defaultMaxPerSecond
          // ignore: cast_nullable_to_non_nullable
          : defaultMaxPerSecond as int?,
    );
  }
}

extension $SinkTypeCopyWith on SinkType {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkType.copyWith(...)` or `instanceOfSinkType.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkTypeCWProxy get copyWith => _$SinkTypeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkType _$SinkTypeFromJson(Map<String, dynamic> json) => $checkedCreate(
  'SinkType',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'id',
        'name',
        'kind',
        'modes',
        'configSchema',
        'secretFields',
      ],
    );
    final val = SinkType(
      id: $checkedConvert('id', (v) => $enumDecode(_$SinkTypeIdEnumEnumMap, v)),
      name: $checkedConvert('name', (v) => v as String),
      description: $checkedConvert('description', (v) => v as String?),
      kind: $checkedConvert(
        'kind',
        (v) => $enumDecode(_$SinkTypeKindEnumEnumMap, v),
      ),
      modes: $checkedConvert(
        'modes',
        (v) => (v as List<dynamic>)
            .map((e) => $enumDecode(_$SinkTypeModesEnumEnumMap, e))
            .toList(),
      ),
      configSchema: $checkedConvert(
        'configSchema',
        (v) =>
            (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
      ),
      secretFields: $checkedConvert(
        'secretFields',
        (v) => (v as List<dynamic>).map((e) => e as String).toList(),
      ),
      defaultMaxPerSecond: $checkedConvert(
        'defaultMaxPerSecond',
        (v) => (v as num?)?.toInt(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$SinkTypeToJson(SinkType instance) => <String, dynamic>{
  'id': _$SinkTypeIdEnumEnumMap[instance.id]!,
  'name': instance.name,
  'description': ?instance.description,
  'kind': _$SinkTypeKindEnumEnumMap[instance.kind]!,
  'modes': instance.modes.map((e) => _$SinkTypeModesEnumEnumMap[e]!).toList(),
  'configSchema': instance.configSchema,
  'secretFields': instance.secretFields,
  'defaultMaxPerSecond': ?instance.defaultMaxPerSecond,
};

const _$SinkTypeIdEnumEnumMap = {
  SinkTypeIdEnum.mqttState: 'mqtt-state',
  SinkTypeIdEnum.cot: 'cot',
  SinkTypeIdEnum.readsbJson: 'readsb-json',
  SinkTypeIdEnum.sbs: 'sbs',
  SinkTypeIdEnum.nmeaAivdm: 'nmea-aivdm',
  SinkTypeIdEnum.aprsIsServe: 'aprs-is-serve',
  SinkTypeIdEnum.cloudevents: 'cloudevents',
  SinkTypeIdEnum.federationPush: 'federation-push',
};

const _$SinkTypeKindEnumEnumMap = {
  SinkTypeKindEnum.CHANGE_FEED: 'CHANGE_FEED',
  SinkTypeKindEnum.STATE_MIRROR: 'STATE_MIRROR',
};

const _$SinkTypeModesEnumEnumMap = {
  SinkTypeModesEnum.PUSH: 'PUSH',
  SinkTypeModesEnum.SERVE: 'SERVE',
};
