// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_health_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestHealthItemCWProxy {
  DigestHealthItem sourceId(String sourceId);

  DigestHealthItem status(DigestHealthItemStatusEnum status);

  DigestHealthItem since(DateTime? since);

  DigestHealthItem lastError(String? lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestHealthItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestHealthItem(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestHealthItem call({
    String sourceId,
    DigestHealthItemStatusEnum status,
    DateTime? since,
    String? lastError,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestHealthItem.copyWith(...)` or call `instanceOfDigestHealthItem.copyWith.fieldName(value)` for a single field.
class _$DigestHealthItemCWProxyImpl implements _$DigestHealthItemCWProxy {
  const _$DigestHealthItemCWProxyImpl(this._value);

  final DigestHealthItem _value;

  @override
  DigestHealthItem sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  DigestHealthItem status(DigestHealthItemStatusEnum status) =>
      call(status: status);

  @override
  DigestHealthItem since(DateTime? since) => call(since: since);

  @override
  DigestHealthItem lastError(String? lastError) => call(lastError: lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestHealthItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestHealthItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestHealthItem call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
  }) {
    return DigestHealthItem(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DigestHealthItemStatusEnum,
      since: since == const $CopyWithPlaceholder()
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
    );
  }
}

extension $DigestHealthItemCopyWith on DigestHealthItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestHealthItem.copyWith(...)` or `instanceOfDigestHealthItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestHealthItemCWProxy get copyWith => _$DigestHealthItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestHealthItem _$DigestHealthItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestHealthItem', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sourceId', 'status']);
      final val = DigestHealthItem(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$DigestHealthItemStatusEnumEnumMap, v),
        ),
        since: $checkedConvert(
          'since',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastError: $checkedConvert('lastError', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$DigestHealthItemToJson(DigestHealthItem instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'status': _$DigestHealthItemStatusEnumEnumMap[instance.status]!,
      'since': ?instance.since?.toIso8601String(),
      'lastError': ?instance.lastError,
    };

const _$DigestHealthItemStatusEnumEnumMap = {
  DigestHealthItemStatusEnum.degraded: 'degraded',
  DigestHealthItemStatusEnum.failing: 'failing',
};
