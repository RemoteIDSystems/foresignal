// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_format_option.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestFormatOptionCWProxy {
  IngestFormatOption name(String name);

  IngestFormatOption type(IngestFormatOptionTypeEnum type);

  IngestFormatOption required_(bool? required_);

  IngestFormatOption default_(Object? default_);

  IngestFormatOption description(String? description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestFormatOption(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestFormatOption(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestFormatOption call({
    String name,
    IngestFormatOptionTypeEnum type,
    bool? required_,
    Object? default_,
    String? description,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestFormatOption.copyWith(...)` or call `instanceOfIngestFormatOption.copyWith.fieldName(value)` for a single field.
class _$IngestFormatOptionCWProxyImpl implements _$IngestFormatOptionCWProxy {
  const _$IngestFormatOptionCWProxyImpl(this._value);

  final IngestFormatOption _value;

  @override
  IngestFormatOption name(String name) => call(name: name);

  @override
  IngestFormatOption type(IngestFormatOptionTypeEnum type) => call(type: type);

  @override
  IngestFormatOption required_(bool? required_) => call(required_: required_);

  @override
  IngestFormatOption default_(Object? default_) => call(default_: default_);

  @override
  IngestFormatOption description(String? description) =>
      call(description: description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestFormatOption(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestFormatOption(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestFormatOption call({
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? required_ = const $CopyWithPlaceholder(),
    Object? default_ = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
  }) {
    return IngestFormatOption(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as IngestFormatOptionTypeEnum,
      required_: required_ == const $CopyWithPlaceholder()
          ? _value.required_
          // ignore: cast_nullable_to_non_nullable
          : required_ as bool?,
      default_: default_ == const $CopyWithPlaceholder()
          ? _value.default_
          // ignore: cast_nullable_to_non_nullable
          : default_ as Object?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
    );
  }
}

extension $IngestFormatOptionCopyWith on IngestFormatOption {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestFormatOption.copyWith(...)` or `instanceOfIngestFormatOption.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestFormatOptionCWProxy get copyWith =>
      _$IngestFormatOptionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestFormatOption _$IngestFormatOptionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestFormatOption', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'type']);
      final val = IngestFormatOption(
        name: $checkedConvert('name', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$IngestFormatOptionTypeEnumEnumMap, v),
        ),
        required_: $checkedConvert('required', (v) => v as bool?),
        default_: $checkedConvert('default', (v) => v),
        description: $checkedConvert('description', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'required_': 'required', 'default_': 'default'});

Map<String, dynamic> _$IngestFormatOptionToJson(IngestFormatOption instance) =>
    <String, dynamic>{
      'name': instance.name,
      'type': _$IngestFormatOptionTypeEnumEnumMap[instance.type]!,
      'required': ?instance.required_,
      'default': ?instance.default_,
      'description': ?instance.description,
    };

const _$IngestFormatOptionTypeEnumEnumMap = {
  IngestFormatOptionTypeEnum.string: 'string',
  IngestFormatOptionTypeEnum.integer: 'integer',
  IngestFormatOptionTypeEnum.number: 'number',
  IngestFormatOptionTypeEnum.boolean: 'boolean',
  IngestFormatOptionTypeEnum.object: 'object',
  IngestFormatOptionTypeEnum.array: 'array',
};
