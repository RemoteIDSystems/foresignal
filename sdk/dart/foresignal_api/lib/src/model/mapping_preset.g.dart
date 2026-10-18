// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_preset.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingPresetCWProxy {
  MappingPreset id(String id);

  MappingPreset title(String title);

  MappingPreset description(String? description);

  MappingPreset format(String format);

  MappingPreset mapping(RecordMapping? mapping);

  MappingPreset kinds(List<String> kinds);

  MappingPreset fieldSpecs(Map<String, FieldSpec>? fieldSpecs);

  MappingPreset observedAtMode(MappingPresetObservedAtModeEnum observedAtMode);

  MappingPreset ttl(String? ttl);

  MappingPreset ingestPolicy(IngestPolicy? ingestPolicy);

  MappingPreset significantFields(List<String>? significantFields);

  MappingPreset volatileFields(List<String>? volatileFields);

  MappingPreset private(bool? private);

  MappingPreset allowPathKey(bool? allowPathKey);

  MappingPreset allowsGet(bool? allowsGet);

  MappingPreset params(List<ParamSpec> params);

  MappingPreset setup(MappingPresetSetup setup);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPreset(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPreset(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingPreset call({
    String id,
    String title,
    String? description,
    String format,
    RecordMapping? mapping,
    List<String> kinds,
    Map<String, FieldSpec>? fieldSpecs,
    MappingPresetObservedAtModeEnum observedAtMode,
    String? ttl,
    IngestPolicy? ingestPolicy,
    List<String>? significantFields,
    List<String>? volatileFields,
    bool? private,
    bool? allowPathKey,
    bool? allowsGet,
    List<ParamSpec> params,
    MappingPresetSetup setup,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingPreset.copyWith(...)` or call `instanceOfMappingPreset.copyWith.fieldName(value)` for a single field.
class _$MappingPresetCWProxyImpl implements _$MappingPresetCWProxy {
  const _$MappingPresetCWProxyImpl(this._value);

  final MappingPreset _value;

  @override
  MappingPreset id(String id) => call(id: id);

  @override
  MappingPreset title(String title) => call(title: title);

  @override
  MappingPreset description(String? description) =>
      call(description: description);

  @override
  MappingPreset format(String format) => call(format: format);

  @override
  MappingPreset mapping(RecordMapping? mapping) => call(mapping: mapping);

  @override
  MappingPreset kinds(List<String> kinds) => call(kinds: kinds);

  @override
  MappingPreset fieldSpecs(Map<String, FieldSpec>? fieldSpecs) =>
      call(fieldSpecs: fieldSpecs);

  @override
  MappingPreset observedAtMode(
    MappingPresetObservedAtModeEnum observedAtMode,
  ) => call(observedAtMode: observedAtMode);

  @override
  MappingPreset ttl(String? ttl) => call(ttl: ttl);

  @override
  MappingPreset ingestPolicy(IngestPolicy? ingestPolicy) =>
      call(ingestPolicy: ingestPolicy);

  @override
  MappingPreset significantFields(List<String>? significantFields) =>
      call(significantFields: significantFields);

  @override
  MappingPreset volatileFields(List<String>? volatileFields) =>
      call(volatileFields: volatileFields);

  @override
  MappingPreset private(bool? private) => call(private: private);

  @override
  MappingPreset allowPathKey(bool? allowPathKey) =>
      call(allowPathKey: allowPathKey);

  @override
  MappingPreset allowsGet(bool? allowsGet) => call(allowsGet: allowsGet);

  @override
  MappingPreset params(List<ParamSpec> params) => call(params: params);

  @override
  MappingPreset setup(MappingPresetSetup setup) => call(setup: setup);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPreset(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPreset(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingPreset call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? mapping = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? fieldSpecs = const $CopyWithPlaceholder(),
    Object? observedAtMode = const $CopyWithPlaceholder(),
    Object? ttl = const $CopyWithPlaceholder(),
    Object? ingestPolicy = const $CopyWithPlaceholder(),
    Object? significantFields = const $CopyWithPlaceholder(),
    Object? volatileFields = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? allowPathKey = const $CopyWithPlaceholder(),
    Object? allowsGet = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? setup = const $CopyWithPlaceholder(),
  }) {
    return MappingPreset(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as String,
      mapping: mapping == const $CopyWithPlaceholder()
          ? _value.mapping
          // ignore: cast_nullable_to_non_nullable
          : mapping as RecordMapping?,
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>,
      fieldSpecs: fieldSpecs == const $CopyWithPlaceholder()
          ? _value.fieldSpecs
          // ignore: cast_nullable_to_non_nullable
          : fieldSpecs as Map<String, FieldSpec>?,
      observedAtMode:
          observedAtMode == const $CopyWithPlaceholder() ||
              observedAtMode == null
          ? _value.observedAtMode
          // ignore: cast_nullable_to_non_nullable
          : observedAtMode as MappingPresetObservedAtModeEnum,
      ttl: ttl == const $CopyWithPlaceholder()
          ? _value.ttl
          // ignore: cast_nullable_to_non_nullable
          : ttl as String?,
      ingestPolicy: ingestPolicy == const $CopyWithPlaceholder()
          ? _value.ingestPolicy
          // ignore: cast_nullable_to_non_nullable
          : ingestPolicy as IngestPolicy?,
      significantFields: significantFields == const $CopyWithPlaceholder()
          ? _value.significantFields
          // ignore: cast_nullable_to_non_nullable
          : significantFields as List<String>?,
      volatileFields: volatileFields == const $CopyWithPlaceholder()
          ? _value.volatileFields
          // ignore: cast_nullable_to_non_nullable
          : volatileFields as List<String>?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      allowPathKey: allowPathKey == const $CopyWithPlaceholder()
          ? _value.allowPathKey
          // ignore: cast_nullable_to_non_nullable
          : allowPathKey as bool?,
      allowsGet: allowsGet == const $CopyWithPlaceholder()
          ? _value.allowsGet
          // ignore: cast_nullable_to_non_nullable
          : allowsGet as bool?,
      params: params == const $CopyWithPlaceholder() || params == null
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as List<ParamSpec>,
      setup: setup == const $CopyWithPlaceholder() || setup == null
          ? _value.setup
          // ignore: cast_nullable_to_non_nullable
          : setup as MappingPresetSetup,
    );
  }
}

extension $MappingPresetCopyWith on MappingPreset {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingPreset.copyWith(...)` or `instanceOfMappingPreset.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingPresetCWProxy get copyWith => _$MappingPresetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingPreset _$MappingPresetFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingPreset', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'title',
      'format',
      'kinds',
      'observedAtMode',
      'params',
      'setup',
    ],
  );
  final val = MappingPreset(
    id: $checkedConvert('id', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    format: $checkedConvert('format', (v) => v as String),
    mapping: $checkedConvert(
      'mapping',
      (v) =>
          v == null ? null : RecordMapping.fromJson(v as Map<String, dynamic>),
    ),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    fieldSpecs: $checkedConvert(
      'fieldSpecs',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, FieldSpec.fromJson(e as Map<String, dynamic>)),
      ),
    ),
    observedAtMode: $checkedConvert(
      'observedAtMode',
      (v) => $enumDecode(_$MappingPresetObservedAtModeEnumEnumMap, v),
    ),
    ttl: $checkedConvert('ttl', (v) => v as String?),
    ingestPolicy: $checkedConvert(
      'ingestPolicy',
      (v) =>
          v == null ? null : IngestPolicy.fromJson(v as Map<String, dynamic>),
    ),
    significantFields: $checkedConvert(
      'significantFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    volatileFields: $checkedConvert(
      'volatileFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    private: $checkedConvert('private', (v) => v as bool?),
    allowPathKey: $checkedConvert('allowPathKey', (v) => v as bool?),
    allowsGet: $checkedConvert('allowsGet', (v) => v as bool?),
    params: $checkedConvert(
      'params',
      (v) => (v as List<dynamic>)
          .map((e) => ParamSpec.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    setup: $checkedConvert(
      'setup',
      (v) => MappingPresetSetup.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$MappingPresetToJson(
  MappingPreset instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': instance.title,
  'description': ?instance.description,
  'format': instance.format,
  'mapping': ?instance.mapping?.toJson(),
  'kinds': instance.kinds,
  'fieldSpecs': ?instance.fieldSpecs?.map((k, e) => MapEntry(k, e.toJson())),
  'observedAtMode':
      _$MappingPresetObservedAtModeEnumEnumMap[instance.observedAtMode]!,
  'ttl': ?instance.ttl,
  'ingestPolicy': ?instance.ingestPolicy?.toJson(),
  'significantFields': ?instance.significantFields,
  'volatileFields': ?instance.volatileFields,
  'private': ?instance.private,
  'allowPathKey': ?instance.allowPathKey,
  'allowsGet': ?instance.allowsGet,
  'params': instance.params.map((e) => e.toJson()).toList(),
  'setup': instance.setup.toJson(),
};

const _$MappingPresetObservedAtModeEnumEnumMap = {
  MappingPresetObservedAtModeEnum.UPSTREAM: 'UPSTREAM',
  MappingPresetObservedAtModeEnum.RECEIVED: 'RECEIVED',
};
