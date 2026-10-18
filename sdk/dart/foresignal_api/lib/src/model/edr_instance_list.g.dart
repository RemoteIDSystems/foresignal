// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edr_instance_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdrInstanceListCWProxy {
  EdrInstanceList instances(List<EdrInstanceListInstancesInner> instances);

  EdrInstanceList links(List<OgcLink>? links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdrInstanceList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdrInstanceList(...).copyWith(id: 12, name: "My name")
  /// ```
  EdrInstanceList call({
    List<EdrInstanceListInstancesInner> instances,
    List<OgcLink>? links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdrInstanceList.copyWith(...)` or call `instanceOfEdrInstanceList.copyWith.fieldName(value)` for a single field.
class _$EdrInstanceListCWProxyImpl implements _$EdrInstanceListCWProxy {
  const _$EdrInstanceListCWProxyImpl(this._value);

  final EdrInstanceList _value;

  @override
  EdrInstanceList instances(List<EdrInstanceListInstancesInner> instances) =>
      call(instances: instances);

  @override
  EdrInstanceList links(List<OgcLink>? links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdrInstanceList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdrInstanceList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdrInstanceList call({
    Object? instances = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return EdrInstanceList(
      instances: instances == const $CopyWithPlaceholder() || instances == null
          ? _value.instances
          // ignore: cast_nullable_to_non_nullable
          : instances as List<EdrInstanceListInstancesInner>,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>?,
    );
  }
}

extension $EdrInstanceListCopyWith on EdrInstanceList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdrInstanceList.copyWith(...)` or `instanceOfEdrInstanceList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdrInstanceListCWProxy get copyWith => _$EdrInstanceListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdrInstanceList _$EdrInstanceListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdrInstanceList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['instances']);
      final val = EdrInstanceList(
        instances: $checkedConvert(
          'instances',
          (v) => (v as List<dynamic>)
              .map(
                (e) => EdrInstanceListInstancesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>?)
              ?.map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdrInstanceListToJson(EdrInstanceList instance) =>
    <String, dynamic>{
      'instances': instance.instances.map((e) => e.toJson()).toList(),
      'links': ?instance.links?.map((e) => e.toJson()).toList(),
    };
