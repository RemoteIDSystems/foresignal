// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactPageCWProxy {
  ProjectedImpactPage items(List<ProjectedImpact> items);

  ProjectedImpactPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactPage call({List<ProjectedImpact> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactPage.copyWith(...)` or call `instanceOfProjectedImpactPage.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactPageCWProxyImpl implements _$ProjectedImpactPageCWProxy {
  const _$ProjectedImpactPageCWProxyImpl(this._value);

  final ProjectedImpactPage _value;

  @override
  ProjectedImpactPage items(List<ProjectedImpact> items) => call(items: items);

  @override
  ProjectedImpactPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ProjectedImpact>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ProjectedImpactPageCopyWith on ProjectedImpactPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactPage.copyWith(...)` or `instanceOfProjectedImpactPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactPageCWProxy get copyWith =>
      _$ProjectedImpactPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactPage _$ProjectedImpactPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProjectedImpactPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ProjectedImpactPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ProjectedImpact.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProjectedImpactPageToJson(
  ProjectedImpactPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
