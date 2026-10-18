// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkCWProxy {
  Sink id(String id);

  Sink type(SinkTypeEnum type);

  Sink kind(SinkKindEnum kind);

  Sink mode(SinkModeEnum mode);

  Sink name(String name);

  Sink enabled(bool enabled);

  Sink config(Map<String, Object> config);

  Sink secretsSet(List<String>? secretsSet);

  Sink filter(SinkFilter filter);

  Sink state(SinkStateEnum? state);

  Sink createdAt(DateTime createdAt);

  Sink updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Sink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Sink(...).copyWith(id: 12, name: "My name")
  /// ```
  Sink call({
    String id,
    SinkTypeEnum type,
    SinkKindEnum kind,
    SinkModeEnum mode,
    String name,
    bool enabled,
    Map<String, Object> config,
    List<String>? secretsSet,
    SinkFilter filter,
    SinkStateEnum? state,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSink.copyWith(...)` or call `instanceOfSink.copyWith.fieldName(value)` for a single field.
class _$SinkCWProxyImpl implements _$SinkCWProxy {
  const _$SinkCWProxyImpl(this._value);

  final Sink _value;

  @override
  Sink id(String id) => call(id: id);

  @override
  Sink type(SinkTypeEnum type) => call(type: type);

  @override
  Sink kind(SinkKindEnum kind) => call(kind: kind);

  @override
  Sink mode(SinkModeEnum mode) => call(mode: mode);

  @override
  Sink name(String name) => call(name: name);

  @override
  Sink enabled(bool enabled) => call(enabled: enabled);

  @override
  Sink config(Map<String, Object> config) => call(config: config);

  @override
  Sink secretsSet(List<String>? secretsSet) => call(secretsSet: secretsSet);

  @override
  Sink filter(SinkFilter filter) => call(filter: filter);

  @override
  Sink state(SinkStateEnum? state) => call(state: state);

  @override
  Sink createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Sink updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Sink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Sink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Sink call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secretsSet = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Sink(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as SinkTypeEnum,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as SinkKindEnum,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as SinkModeEnum,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      config: config == const $CopyWithPlaceholder() || config == null
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>,
      secretsSet: secretsSet == const $CopyWithPlaceholder()
          ? _value.secretsSet
          // ignore: cast_nullable_to_non_nullable
          : secretsSet as List<String>?,
      filter: filter == const $CopyWithPlaceholder() || filter == null
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as SinkFilter,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as SinkStateEnum?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $SinkCopyWith on Sink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSink.copyWith(...)` or `instanceOfSink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkCWProxy get copyWith => _$SinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Sink _$SinkFromJson(Map<String, dynamic> json) => $checkedCreate('Sink', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'type',
      'kind',
      'mode',
      'name',
      'enabled',
      'config',
      'filter',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Sink(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert('type', (v) => $enumDecode(_$SinkTypeEnumEnumMap, v)),
    kind: $checkedConvert('kind', (v) => $enumDecode(_$SinkKindEnumEnumMap, v)),
    mode: $checkedConvert('mode', (v) => $enumDecode(_$SinkModeEnumEnumMap, v)),
    name: $checkedConvert('name', (v) => v as String),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    secretsSet: $checkedConvert(
      'secretsSet',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    filter: $checkedConvert(
      'filter',
      (v) => SinkFilter.fromJson(v as Map<String, dynamic>),
    ),
    state: $checkedConvert(
      'state',
      (v) => $enumDecodeNullable(_$SinkStateEnumEnumMap, v),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$SinkToJson(Sink instance) => <String, dynamic>{
  'id': instance.id,
  'type': _$SinkTypeEnumEnumMap[instance.type]!,
  'kind': _$SinkKindEnumEnumMap[instance.kind]!,
  'mode': _$SinkModeEnumEnumMap[instance.mode]!,
  'name': instance.name,
  'enabled': instance.enabled,
  'config': instance.config,
  'secretsSet': ?instance.secretsSet,
  'filter': instance.filter.toJson(),
  'state': ?_$SinkStateEnumEnumMap[instance.state],
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$SinkTypeEnumEnumMap = {
  SinkTypeEnum.mqttState: 'mqtt-state',
  SinkTypeEnum.cot: 'cot',
  SinkTypeEnum.readsbJson: 'readsb-json',
  SinkTypeEnum.sbs: 'sbs',
  SinkTypeEnum.nmeaAivdm: 'nmea-aivdm',
  SinkTypeEnum.aprsIsServe: 'aprs-is-serve',
  SinkTypeEnum.cloudevents: 'cloudevents',
  SinkTypeEnum.federationPush: 'federation-push',
};

const _$SinkKindEnumEnumMap = {
  SinkKindEnum.CHANGE_FEED: 'CHANGE_FEED',
  SinkKindEnum.STATE_MIRROR: 'STATE_MIRROR',
};

const _$SinkModeEnumEnumMap = {
  SinkModeEnum.PUSH: 'PUSH',
  SinkModeEnum.SERVE: 'SERVE',
};

const _$SinkStateEnumEnumMap = {
  SinkStateEnum.RUNNING: 'RUNNING',
  SinkStateEnum.RESYNCING: 'RESYNCING',
  SinkStateEnum.FAILING: 'FAILING',
  SinkStateEnum.STOPPED: 'STOPPED',
};
