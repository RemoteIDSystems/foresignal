// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_match_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaMatchRequestCWProxy {
  MediaMatchRequest pdq(String pdq);

  MediaMatchRequest maxDistance(int? maxDistance);

  MediaMatchRequest limit(int? limit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatchRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatchRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaMatchRequest call({String pdq, int? maxDistance, int? limit});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaMatchRequest.copyWith(...)` or call `instanceOfMediaMatchRequest.copyWith.fieldName(value)` for a single field.
class _$MediaMatchRequestCWProxyImpl implements _$MediaMatchRequestCWProxy {
  const _$MediaMatchRequestCWProxyImpl(this._value);

  final MediaMatchRequest _value;

  @override
  MediaMatchRequest pdq(String pdq) => call(pdq: pdq);

  @override
  MediaMatchRequest maxDistance(int? maxDistance) =>
      call(maxDistance: maxDistance);

  @override
  MediaMatchRequest limit(int? limit) => call(limit: limit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaMatchRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaMatchRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaMatchRequest call({
    Object? pdq = const $CopyWithPlaceholder(),
    Object? maxDistance = const $CopyWithPlaceholder(),
    Object? limit = const $CopyWithPlaceholder(),
  }) {
    return MediaMatchRequest(
      pdq: pdq == const $CopyWithPlaceholder() || pdq == null
          ? _value.pdq
          // ignore: cast_nullable_to_non_nullable
          : pdq as String,
      maxDistance: maxDistance == const $CopyWithPlaceholder()
          ? _value.maxDistance
          // ignore: cast_nullable_to_non_nullable
          : maxDistance as int?,
      limit: limit == const $CopyWithPlaceholder()
          ? _value.limit
          // ignore: cast_nullable_to_non_nullable
          : limit as int?,
    );
  }
}

extension $MediaMatchRequestCopyWith on MediaMatchRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaMatchRequest.copyWith(...)` or `instanceOfMediaMatchRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaMatchRequestCWProxy get copyWith =>
      _$MediaMatchRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaMatchRequest _$MediaMatchRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaMatchRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['pdq']);
      final val = MediaMatchRequest(
        pdq: $checkedConvert('pdq', (v) => v as String),
        maxDistance: $checkedConvert(
          'maxDistance',
          (v) => (v as num?)?.toInt() ?? 31,
        ),
        limit: $checkedConvert('limit', (v) => (v as num?)?.toInt() ?? 20),
      );
      return val;
    });

Map<String, dynamic> _$MediaMatchRequestToJson(MediaMatchRequest instance) =>
    <String, dynamic>{
      'pdq': instance.pdq,
      'maxDistance': ?instance.maxDistance,
      'limit': ?instance.limit,
    };
