// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_edge_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraEdgePageCWProxy {
  InfraEdgePage items(List<InfraEdge> items);

  InfraEdgePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraEdgePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraEdgePage(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraEdgePage call({List<InfraEdge> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraEdgePage.copyWith(...)` or call `instanceOfInfraEdgePage.copyWith.fieldName(value)` for a single field.
class _$InfraEdgePageCWProxyImpl implements _$InfraEdgePageCWProxy {
  const _$InfraEdgePageCWProxyImpl(this._value);

  final InfraEdgePage _value;

  @override
  InfraEdgePage items(List<InfraEdge> items) => call(items: items);

  @override
  InfraEdgePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraEdgePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraEdgePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraEdgePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return InfraEdgePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<InfraEdge>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $InfraEdgePageCopyWith on InfraEdgePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraEdgePage.copyWith(...)` or `instanceOfInfraEdgePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraEdgePageCWProxy get copyWith => _$InfraEdgePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraEdgePage _$InfraEdgePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraEdgePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = InfraEdgePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => InfraEdge.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$InfraEdgePageToJson(InfraEdgePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
