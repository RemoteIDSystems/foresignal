// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_spec.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldSpecCWProxy {
  FieldSpec type(FieldSpecTypeEnum type);

  FieldSpec unit(String? unit);

  FieldSpec unitLabel(String? unitLabel);

  FieldSpec label(String? label);

  FieldSpec description(String? description);

  FieldSpec datum(String? datum);

  FieldSpec format(String? format);

  FieldSpec enumLabels(Map<String, String>? enumLabels);

  FieldSpec kinds(List<String>? kinds);

  FieldSpec noLink(bool? noLink);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldSpec call({
    FieldSpecTypeEnum type,
    String? unit,
    String? unitLabel,
    String? label,
    String? description,
    String? datum,
    String? format,
    Map<String, String>? enumLabels,
    List<String>? kinds,
    bool? noLink,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldSpec.copyWith(...)` or call `instanceOfFieldSpec.copyWith.fieldName(value)` for a single field.
class _$FieldSpecCWProxyImpl implements _$FieldSpecCWProxy {
  const _$FieldSpecCWProxyImpl(this._value);

  final FieldSpec _value;

  @override
  FieldSpec type(FieldSpecTypeEnum type) => call(type: type);

  @override
  FieldSpec unit(String? unit) => call(unit: unit);

  @override
  FieldSpec unitLabel(String? unitLabel) => call(unitLabel: unitLabel);

  @override
  FieldSpec label(String? label) => call(label: label);

  @override
  FieldSpec description(String? description) => call(description: description);

  @override
  FieldSpec datum(String? datum) => call(datum: datum);

  @override
  FieldSpec format(String? format) => call(format: format);

  @override
  FieldSpec enumLabels(Map<String, String>? enumLabels) =>
      call(enumLabels: enumLabels);

  @override
  FieldSpec kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  FieldSpec noLink(bool? noLink) => call(noLink: noLink);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldSpec call({
    Object? type = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? unitLabel = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? datum = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? enumLabels = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? noLink = const $CopyWithPlaceholder(),
  }) {
    return FieldSpec(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as FieldSpecTypeEnum,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      unitLabel: unitLabel == const $CopyWithPlaceholder()
          ? _value.unitLabel
          // ignore: cast_nullable_to_non_nullable
          : unitLabel as String?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      datum: datum == const $CopyWithPlaceholder()
          ? _value.datum
          // ignore: cast_nullable_to_non_nullable
          : datum as String?,
      format: format == const $CopyWithPlaceholder()
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as String?,
      enumLabels: enumLabels == const $CopyWithPlaceholder()
          ? _value.enumLabels
          // ignore: cast_nullable_to_non_nullable
          : enumLabels as Map<String, String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      noLink: noLink == const $CopyWithPlaceholder()
          ? _value.noLink
          // ignore: cast_nullable_to_non_nullable
          : noLink as bool?,
    );
  }
}

extension $FieldSpecCopyWith on FieldSpec {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldSpec.copyWith(...)` or `instanceOfFieldSpec.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldSpecCWProxy get copyWith => _$FieldSpecCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldSpec _$FieldSpecFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FieldSpec', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = FieldSpec(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$FieldSpecTypeEnumEnumMap, v),
        ),
        unit: $checkedConvert('unit', (v) => v as String?),
        unitLabel: $checkedConvert('unitLabel', (v) => v as String?),
        label: $checkedConvert('label', (v) => v as String?),
        description: $checkedConvert('description', (v) => v as String?),
        datum: $checkedConvert('datum', (v) => v as String?),
        format: $checkedConvert('format', (v) => v as String?),
        enumLabels: $checkedConvert(
          'enumLabels',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as String),
          ),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        noLink: $checkedConvert('noLink', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$FieldSpecToJson(FieldSpec instance) => <String, dynamic>{
  'type': _$FieldSpecTypeEnumEnumMap[instance.type]!,
  'unit': ?instance.unit,
  'unitLabel': ?instance.unitLabel,
  'label': ?instance.label,
  'description': ?instance.description,
  'datum': ?instance.datum,
  'format': ?instance.format,
  'enumLabels': ?instance.enumLabels,
  'kinds': ?instance.kinds,
  'noLink': ?instance.noLink,
};

const _$FieldSpecTypeEnumEnumMap = {
  FieldSpecTypeEnum.STRING: 'STRING',
  FieldSpecTypeEnum.INTEGER: 'INTEGER',
  FieldSpecTypeEnum.NUMBER: 'NUMBER',
  FieldSpecTypeEnum.BOOLEAN: 'BOOLEAN',
  FieldSpecTypeEnum.TIMESTAMP: 'TIMESTAMP',
  FieldSpecTypeEnum.OBJECT: 'OBJECT',
  FieldSpecTypeEnum.ARRAY: 'ARRAY',
};
