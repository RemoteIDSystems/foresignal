// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_format_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestFormatInfoCWProxy {
  IngestFormatInfo id(String id);

  IngestFormatInfo description(String? description);

  IngestFormatInfo mediaTypes(List<String> mediaTypes);

  IngestFormatInfo methods(List<IngestFormatInfoMethodsEnum>? methods);

  IngestFormatInfo options(List<IngestFormatOption>? options);

  IngestFormatInfo verifiers(List<IngestFormatInfoVerifiersEnum>? verifiers);

  IngestFormatInfo reserved(bool? reserved);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestFormatInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestFormatInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestFormatInfo call({
    String id,
    String? description,
    List<String> mediaTypes,
    List<IngestFormatInfoMethodsEnum>? methods,
    List<IngestFormatOption>? options,
    List<IngestFormatInfoVerifiersEnum>? verifiers,
    bool? reserved,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestFormatInfo.copyWith(...)` or call `instanceOfIngestFormatInfo.copyWith.fieldName(value)` for a single field.
class _$IngestFormatInfoCWProxyImpl implements _$IngestFormatInfoCWProxy {
  const _$IngestFormatInfoCWProxyImpl(this._value);

  final IngestFormatInfo _value;

  @override
  IngestFormatInfo id(String id) => call(id: id);

  @override
  IngestFormatInfo description(String? description) =>
      call(description: description);

  @override
  IngestFormatInfo mediaTypes(List<String> mediaTypes) =>
      call(mediaTypes: mediaTypes);

  @override
  IngestFormatInfo methods(List<IngestFormatInfoMethodsEnum>? methods) =>
      call(methods: methods);

  @override
  IngestFormatInfo options(List<IngestFormatOption>? options) =>
      call(options: options);

  @override
  IngestFormatInfo verifiers(List<IngestFormatInfoVerifiersEnum>? verifiers) =>
      call(verifiers: verifiers);

  @override
  IngestFormatInfo reserved(bool? reserved) => call(reserved: reserved);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestFormatInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestFormatInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestFormatInfo call({
    Object? id = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? mediaTypes = const $CopyWithPlaceholder(),
    Object? methods = const $CopyWithPlaceholder(),
    Object? options = const $CopyWithPlaceholder(),
    Object? verifiers = const $CopyWithPlaceholder(),
    Object? reserved = const $CopyWithPlaceholder(),
  }) {
    return IngestFormatInfo(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      mediaTypes:
          mediaTypes == const $CopyWithPlaceholder() || mediaTypes == null
          ? _value.mediaTypes
          // ignore: cast_nullable_to_non_nullable
          : mediaTypes as List<String>,
      methods: methods == const $CopyWithPlaceholder()
          ? _value.methods
          // ignore: cast_nullable_to_non_nullable
          : methods as List<IngestFormatInfoMethodsEnum>?,
      options: options == const $CopyWithPlaceholder()
          ? _value.options
          // ignore: cast_nullable_to_non_nullable
          : options as List<IngestFormatOption>?,
      verifiers: verifiers == const $CopyWithPlaceholder()
          ? _value.verifiers
          // ignore: cast_nullable_to_non_nullable
          : verifiers as List<IngestFormatInfoVerifiersEnum>?,
      reserved: reserved == const $CopyWithPlaceholder()
          ? _value.reserved
          // ignore: cast_nullable_to_non_nullable
          : reserved as bool?,
    );
  }
}

extension $IngestFormatInfoCopyWith on IngestFormatInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestFormatInfo.copyWith(...)` or `instanceOfIngestFormatInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestFormatInfoCWProxy get copyWith => _$IngestFormatInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestFormatInfo _$IngestFormatInfoFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestFormatInfo', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'mediaTypes']);
  final val = IngestFormatInfo(
    id: $checkedConvert('id', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    mediaTypes: $checkedConvert(
      'mediaTypes',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    methods: $checkedConvert(
      'methods',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$IngestFormatInfoMethodsEnumEnumMap, e))
          .toList(),
    ),
    options: $checkedConvert(
      'options',
      (v) => (v as List<dynamic>?)
          ?.map((e) => IngestFormatOption.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    verifiers: $checkedConvert(
      'verifiers',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$IngestFormatInfoVerifiersEnumEnumMap, e))
          .toList(),
    ),
    reserved: $checkedConvert('reserved', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$IngestFormatInfoToJson(IngestFormatInfo instance) =>
    <String, dynamic>{
      'id': instance.id,
      'description': ?instance.description,
      'mediaTypes': instance.mediaTypes,
      'methods': ?instance.methods
          ?.map((e) => _$IngestFormatInfoMethodsEnumEnumMap[e]!)
          .toList(),
      'options': ?instance.options?.map((e) => e.toJson()).toList(),
      'verifiers': ?instance.verifiers
          ?.map((e) => _$IngestFormatInfoVerifiersEnumEnumMap[e]!)
          .toList(),
      'reserved': ?instance.reserved,
    };

const _$IngestFormatInfoMethodsEnumEnumMap = {
  IngestFormatInfoMethodsEnum.GET: 'GET',
  IngestFormatInfoMethodsEnum.POST: 'POST',
  IngestFormatInfoMethodsEnum.PUT: 'PUT',
};

const _$IngestFormatInfoVerifiersEnumEnumMap = {
  IngestFormatInfoVerifiersEnum.none: 'none',
  IngestFormatInfoVerifiersEnum.hmacSha256: 'hmac-sha256',
  IngestFormatInfoVerifiersEnum.standardWebhooks: 'standard-webhooks',
  IngestFormatInfoVerifiersEnum.foresignal: 'foresignal',
};
