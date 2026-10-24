// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecaster.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecasterCWProxy {
  Forecaster id(String id);

  Forecaster type(String type);

  Forecaster name(String name);

  Forecaster config(Map<String, Object>? config);

  Forecaster version(String version);

  Forecaster active(bool active);

  Forecaster trackRecord(ForecasterTrackRecord? trackRecord);

  Forecaster createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Forecaster(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Forecaster(...).copyWith(id: 12, name: "My name")
  /// ```
  Forecaster call({
    String id,
    String type,
    String name,
    Map<String, Object>? config,
    String version,
    bool active,
    ForecasterTrackRecord? trackRecord,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecaster.copyWith(...)` or call `instanceOfForecaster.copyWith.fieldName(value)` for a single field.
class _$ForecasterCWProxyImpl implements _$ForecasterCWProxy {
  const _$ForecasterCWProxyImpl(this._value);

  final Forecaster _value;

  @override
  Forecaster id(String id) => call(id: id);

  @override
  Forecaster type(String type) => call(type: type);

  @override
  Forecaster name(String name) => call(name: name);

  @override
  Forecaster config(Map<String, Object>? config) => call(config: config);

  @override
  Forecaster version(String version) => call(version: version);

  @override
  Forecaster active(bool active) => call(active: active);

  @override
  Forecaster trackRecord(ForecasterTrackRecord? trackRecord) =>
      call(trackRecord: trackRecord);

  @override
  Forecaster createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Forecaster(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Forecaster(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Forecaster call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? active = const $CopyWithPlaceholder(),
    Object? trackRecord = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return Forecaster(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      active: active == const $CopyWithPlaceholder() || active == null
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as bool,
      trackRecord: trackRecord == const $CopyWithPlaceholder()
          ? _value.trackRecord
          // ignore: cast_nullable_to_non_nullable
          : trackRecord as ForecasterTrackRecord?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $ForecasterCopyWith on Forecaster {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecaster.copyWith(...)` or `instanceOfForecaster.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecasterCWProxy get copyWith => _$ForecasterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Forecaster _$ForecasterFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Forecaster', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'type',
      'name',
      'version',
      'active',
      'createdAt',
    ],
  );
  final val = Forecaster(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert('type', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    version: $checkedConvert('version', (v) => v as String),
    active: $checkedConvert('active', (v) => v as bool),
    trackRecord: $checkedConvert(
      'trackRecord',
      (v) => v == null
          ? null
          : ForecasterTrackRecord.fromJson(v as Map<String, dynamic>),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ForecasterToJson(Forecaster instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': instance.type,
      'name': instance.name,
      'config': ?instance.config,
      'version': instance.version,
      'active': instance.active,
      'trackRecord': ?instance.trackRecord?.toJson(),
      'createdAt': instance.createdAt.toIso8601String(),
    };
