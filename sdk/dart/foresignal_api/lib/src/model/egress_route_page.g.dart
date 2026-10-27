// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_route_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressRoutePageCWProxy {
  EgressRoutePage items(List<EgressRoute> items);

  EgressRoutePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRoutePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRoutePage(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressRoutePage call({List<EgressRoute> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressRoutePage.copyWith(...)` or call `instanceOfEgressRoutePage.copyWith.fieldName(value)` for a single field.
class _$EgressRoutePageCWProxyImpl implements _$EgressRoutePageCWProxy {
  const _$EgressRoutePageCWProxyImpl(this._value);

  final EgressRoutePage _value;

  @override
  EgressRoutePage items(List<EgressRoute> items) => call(items: items);

  @override
  EgressRoutePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRoutePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRoutePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressRoutePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return EgressRoutePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<EgressRoute>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $EgressRoutePageCopyWith on EgressRoutePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressRoutePage.copyWith(...)` or `instanceOfEgressRoutePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressRoutePageCWProxy get copyWith => _$EgressRoutePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressRoutePage _$EgressRoutePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EgressRoutePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = EgressRoutePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => EgressRoute.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$EgressRoutePageToJson(EgressRoutePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
