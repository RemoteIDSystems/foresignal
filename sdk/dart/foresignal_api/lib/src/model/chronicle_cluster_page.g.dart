// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_cluster_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleClusterPageCWProxy {
  ChronicleClusterPage items(List<ChronicleCluster> items);

  ChronicleClusterPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleClusterPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleClusterPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleClusterPage call({List<ChronicleCluster> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleClusterPage.copyWith(...)` or call `instanceOfChronicleClusterPage.copyWith.fieldName(value)` for a single field.
class _$ChronicleClusterPageCWProxyImpl
    implements _$ChronicleClusterPageCWProxy {
  const _$ChronicleClusterPageCWProxyImpl(this._value);

  final ChronicleClusterPage _value;

  @override
  ChronicleClusterPage items(List<ChronicleCluster> items) =>
      call(items: items);

  @override
  ChronicleClusterPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleClusterPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleClusterPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleClusterPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ChronicleClusterPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ChronicleCluster>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ChronicleClusterPageCopyWith on ChronicleClusterPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleClusterPage.copyWith(...)` or `instanceOfChronicleClusterPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleClusterPageCWProxy get copyWith =>
      _$ChronicleClusterPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleClusterPage _$ChronicleClusterPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleClusterPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
  final val = ChronicleClusterPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => ChronicleCluster.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleClusterPageToJson(
  ChronicleClusterPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
