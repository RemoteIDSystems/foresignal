// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'captured_sample.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CapturedSampleCWProxy {
  CapturedSample receivedAt(DateTime receivedAt);

  CapturedSample method(CapturedSampleMethodEnum method);

  CapturedSample format(String? format);

  CapturedSample contentType(String? contentType);

  CapturedSample contentEncoding(String? contentEncoding);

  CapturedSample query(String? query);

  CapturedSample body(String? body);

  CapturedSample bodyBytes(int? bodyBytes);

  CapturedSample truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CapturedSample(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CapturedSample(...).copyWith(id: 12, name: "My name")
  /// ```
  CapturedSample call({
    DateTime receivedAt,
    CapturedSampleMethodEnum method,
    String? format,
    String? contentType,
    String? contentEncoding,
    String? query,
    String? body,
    int? bodyBytes,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCapturedSample.copyWith(...)` or call `instanceOfCapturedSample.copyWith.fieldName(value)` for a single field.
class _$CapturedSampleCWProxyImpl implements _$CapturedSampleCWProxy {
  const _$CapturedSampleCWProxyImpl(this._value);

  final CapturedSample _value;

  @override
  CapturedSample receivedAt(DateTime receivedAt) =>
      call(receivedAt: receivedAt);

  @override
  CapturedSample method(CapturedSampleMethodEnum method) =>
      call(method: method);

  @override
  CapturedSample format(String? format) => call(format: format);

  @override
  CapturedSample contentType(String? contentType) =>
      call(contentType: contentType);

  @override
  CapturedSample contentEncoding(String? contentEncoding) =>
      call(contentEncoding: contentEncoding);

  @override
  CapturedSample query(String? query) => call(query: query);

  @override
  CapturedSample body(String? body) => call(body: body);

  @override
  CapturedSample bodyBytes(int? bodyBytes) => call(bodyBytes: bodyBytes);

  @override
  CapturedSample truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CapturedSample(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CapturedSample(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CapturedSample call({
    Object? receivedAt = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? contentEncoding = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? bodyBytes = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return CapturedSample(
      receivedAt:
          receivedAt == const $CopyWithPlaceholder() || receivedAt == null
          ? _value.receivedAt
          // ignore: cast_nullable_to_non_nullable
          : receivedAt as DateTime,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as CapturedSampleMethodEnum,
      format: format == const $CopyWithPlaceholder()
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as String?,
      contentType: contentType == const $CopyWithPlaceholder()
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String?,
      contentEncoding: contentEncoding == const $CopyWithPlaceholder()
          ? _value.contentEncoding
          // ignore: cast_nullable_to_non_nullable
          : contentEncoding as String?,
      query: query == const $CopyWithPlaceholder()
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as String?,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      bodyBytes: bodyBytes == const $CopyWithPlaceholder()
          ? _value.bodyBytes
          // ignore: cast_nullable_to_non_nullable
          : bodyBytes as int?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $CapturedSampleCopyWith on CapturedSample {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCapturedSample.copyWith(...)` or `instanceOfCapturedSample.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CapturedSampleCWProxy get copyWith => _$CapturedSampleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CapturedSample _$CapturedSampleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CapturedSample', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['receivedAt', 'method']);
      final val = CapturedSample(
        receivedAt: $checkedConvert(
          'receivedAt',
          (v) => DateTime.parse(v as String),
        ),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$CapturedSampleMethodEnumEnumMap, v),
        ),
        format: $checkedConvert('format', (v) => v as String?),
        contentType: $checkedConvert('contentType', (v) => v as String?),
        contentEncoding: $checkedConvert(
          'contentEncoding',
          (v) => v as String?,
        ),
        query: $checkedConvert('query', (v) => v as String?),
        body: $checkedConvert('body', (v) => v as String?),
        bodyBytes: $checkedConvert('bodyBytes', (v) => (v as num?)?.toInt()),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$CapturedSampleToJson(CapturedSample instance) =>
    <String, dynamic>{
      'receivedAt': instance.receivedAt.toIso8601String(),
      'method': _$CapturedSampleMethodEnumEnumMap[instance.method]!,
      'format': ?instance.format,
      'contentType': ?instance.contentType,
      'contentEncoding': ?instance.contentEncoding,
      'query': ?instance.query,
      'body': ?instance.body,
      'bodyBytes': ?instance.bodyBytes,
      'truncated': ?instance.truncated,
    };

const _$CapturedSampleMethodEnumEnumMap = {
  CapturedSampleMethodEnum.GET: 'GET',
  CapturedSampleMethodEnum.POST: 'POST',
  CapturedSampleMethodEnum.PUT: 'PUT',
};
