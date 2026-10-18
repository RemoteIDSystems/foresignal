// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cluster_timeline_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClusterTimelinePageCWProxy {
  ClusterTimelinePage items(List<ClusterTimelineItem> items);

  ClusterTimelinePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClusterTimelinePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClusterTimelinePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ClusterTimelinePage call({
    List<ClusterTimelineItem> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClusterTimelinePage.copyWith(...)` or call `instanceOfClusterTimelinePage.copyWith.fieldName(value)` for a single field.
class _$ClusterTimelinePageCWProxyImpl implements _$ClusterTimelinePageCWProxy {
  const _$ClusterTimelinePageCWProxyImpl(this._value);

  final ClusterTimelinePage _value;

  @override
  ClusterTimelinePage items(List<ClusterTimelineItem> items) =>
      call(items: items);

  @override
  ClusterTimelinePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ClusterTimelinePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ClusterTimelinePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ClusterTimelinePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ClusterTimelinePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ClusterTimelineItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ClusterTimelinePageCopyWith on ClusterTimelinePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClusterTimelinePage.copyWith(...)` or `instanceOfClusterTimelinePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClusterTimelinePageCWProxy get copyWith =>
      _$ClusterTimelinePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClusterTimelinePage _$ClusterTimelinePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClusterTimelinePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ClusterTimelinePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ClusterTimelineItem.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ClusterTimelinePageToJson(
  ClusterTimelinePage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
