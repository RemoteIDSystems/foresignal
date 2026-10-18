// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kind_schema.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KindSchemaCWProxy {
  KindSchema dollarSchema(String? dollarSchema);

  KindSchema title(String? title);

  KindSchema type(String? type);

  KindSchema properties(Map<String, Object>? properties);

  KindSchema xDisplay(KindDisplay? xDisplay);

  KindSchema xConflicts(List<String>? xConflicts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KindSchema(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KindSchema(...).copyWith(id: 12, name: "My name")
  /// ```
  KindSchema call({
    String? dollarSchema,
    String? title,
    String? type,
    Map<String, Object>? properties,
    KindDisplay? xDisplay,
    List<String>? xConflicts,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKindSchema.copyWith(...)` or call `instanceOfKindSchema.copyWith.fieldName(value)` for a single field.
class _$KindSchemaCWProxyImpl implements _$KindSchemaCWProxy {
  const _$KindSchemaCWProxyImpl(this._value);

  final KindSchema _value;

  @override
  KindSchema dollarSchema(String? dollarSchema) =>
      call(dollarSchema: dollarSchema);

  @override
  KindSchema title(String? title) => call(title: title);

  @override
  KindSchema type(String? type) => call(type: type);

  @override
  KindSchema properties(Map<String, Object>? properties) =>
      call(properties: properties);

  @override
  KindSchema xDisplay(KindDisplay? xDisplay) => call(xDisplay: xDisplay);

  @override
  KindSchema xConflicts(List<String>? xConflicts) =>
      call(xConflicts: xConflicts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KindSchema(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KindSchema(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KindSchema call({
    Object? dollarSchema = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? xDisplay = const $CopyWithPlaceholder(),
    Object? xConflicts = const $CopyWithPlaceholder(),
  }) {
    return KindSchema(
      dollarSchema: dollarSchema == const $CopyWithPlaceholder()
          ? _value.dollarSchema
          // ignore: cast_nullable_to_non_nullable
          : dollarSchema as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String?,
      properties: properties == const $CopyWithPlaceholder()
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Object>?,
      xDisplay: xDisplay == const $CopyWithPlaceholder()
          ? _value.xDisplay
          // ignore: cast_nullable_to_non_nullable
          : xDisplay as KindDisplay?,
      xConflicts: xConflicts == const $CopyWithPlaceholder()
          ? _value.xConflicts
          // ignore: cast_nullable_to_non_nullable
          : xConflicts as List<String>?,
    );
  }
}

extension $KindSchemaCopyWith on KindSchema {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKindSchema.copyWith(...)` or `instanceOfKindSchema.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KindSchemaCWProxy get copyWith => _$KindSchemaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KindSchema _$KindSchemaFromJson(Map<String, dynamic> json) => $checkedCreate(
  'KindSchema',
  json,
  ($checkedConvert) {
    final val = KindSchema(
      dollarSchema: $checkedConvert(r'$schema', (v) => v as String?),
      title: $checkedConvert('title', (v) => v as String?),
      type: $checkedConvert('type', (v) => v as String?),
      properties: $checkedConvert(
        'properties',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(k, e as Object),
        ),
      ),
      xDisplay: $checkedConvert(
        'x-display',
        (v) =>
            v == null ? null : KindDisplay.fromJson(v as Map<String, dynamic>),
      ),
      xConflicts: $checkedConvert(
        'x-conflicts',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
    );
    return val;
  },
  fieldKeyMap: const {
    'dollarSchema': r'$schema',
    'xDisplay': 'x-display',
    'xConflicts': 'x-conflicts',
  },
);

Map<String, dynamic> _$KindSchemaToJson(KindSchema instance) =>
    <String, dynamic>{
      r'$schema': ?instance.dollarSchema,
      'title': ?instance.title,
      'type': ?instance.type,
      'properties': ?instance.properties,
      'x-display': ?instance.xDisplay?.toJson(),
      'x-conflicts': ?instance.xConflicts,
    };
