// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_partition_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchivePartitionPageCWProxy {
  ArchivePartitionPage items(List<ArchivePartition> items);

  ArchivePartitionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePartitionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePartitionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchivePartitionPage call({List<ArchivePartition> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchivePartitionPage.copyWith(...)` or call `instanceOfArchivePartitionPage.copyWith.fieldName(value)` for a single field.
class _$ArchivePartitionPageCWProxyImpl
    implements _$ArchivePartitionPageCWProxy {
  const _$ArchivePartitionPageCWProxyImpl(this._value);

  final ArchivePartitionPage _value;

  @override
  ArchivePartitionPage items(List<ArchivePartition> items) =>
      call(items: items);

  @override
  ArchivePartitionPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePartitionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePartitionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchivePartitionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ArchivePartitionPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ArchivePartition>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ArchivePartitionPageCopyWith on ArchivePartitionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchivePartitionPage.copyWith(...)` or `instanceOfArchivePartitionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchivePartitionPageCWProxy get copyWith =>
      _$ArchivePartitionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchivePartitionPage _$ArchivePartitionPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ArchivePartitionPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = ArchivePartitionPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => ArchivePartition.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ArchivePartitionPageToJson(
  ArchivePartitionPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
