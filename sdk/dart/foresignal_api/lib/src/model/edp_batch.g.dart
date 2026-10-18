// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_batch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpBatchCWProxy {
  EdpBatch protocol(String protocol);

  EdpBatch sourceId(String? sourceId);

  EdpBatch sentAt(DateTime? sentAt);

  EdpBatch items(List<EdpItem> items);

  EdpBatch nextCursor(String? nextCursor);

  EdpBatch snapshot(EdpSnapshot? snapshot);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpBatch call({
    String protocol,
    String? sourceId,
    DateTime? sentAt,
    List<EdpItem> items,
    String? nextCursor,
    EdpSnapshot? snapshot,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpBatch.copyWith(...)` or call `instanceOfEdpBatch.copyWith.fieldName(value)` for a single field.
class _$EdpBatchCWProxyImpl implements _$EdpBatchCWProxy {
  const _$EdpBatchCWProxyImpl(this._value);

  final EdpBatch _value;

  @override
  EdpBatch protocol(String protocol) => call(protocol: protocol);

  @override
  EdpBatch sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  EdpBatch sentAt(DateTime? sentAt) => call(sentAt: sentAt);

  @override
  EdpBatch items(List<EdpItem> items) => call(items: items);

  @override
  EdpBatch nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  @override
  EdpBatch snapshot(EdpSnapshot? snapshot) => call(snapshot: snapshot);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpBatch call({
    Object? protocol = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? sentAt = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
  }) {
    return EdpBatch(
      protocol: protocol == const $CopyWithPlaceholder() || protocol == null
          ? _value.protocol
          // ignore: cast_nullable_to_non_nullable
          : protocol as String,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      sentAt: sentAt == const $CopyWithPlaceholder()
          ? _value.sentAt
          // ignore: cast_nullable_to_non_nullable
          : sentAt as DateTime?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<EdpItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as EdpSnapshot?,
    );
  }
}

extension $EdpBatchCopyWith on EdpBatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpBatch.copyWith(...)` or `instanceOfEdpBatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpBatchCWProxy get copyWith => _$EdpBatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpBatch _$EdpBatchFromJson(Map<String, dynamic> json) => $checkedCreate(
  'EdpBatch',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['protocol', 'items']);
    final val = EdpBatch(
      protocol: $checkedConvert('protocol', (v) => v as String),
      sourceId: $checkedConvert('sourceId', (v) => v as String?),
      sentAt: $checkedConvert(
        'sentAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      items: $checkedConvert(
        'items',
        (v) => (v as List<dynamic>)
            .map((e) => EdpItem.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      snapshot: $checkedConvert(
        'snapshot',
        (v) =>
            v == null ? null : EdpSnapshot.fromJson(v as Map<String, dynamic>),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$EdpBatchToJson(EdpBatch instance) => <String, dynamic>{
  'protocol': instance.protocol,
  'sourceId': ?instance.sourceId,
  'sentAt': ?instance.sentAt?.toIso8601String(),
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
  'snapshot': ?instance.snapshot?.toJson(),
};
