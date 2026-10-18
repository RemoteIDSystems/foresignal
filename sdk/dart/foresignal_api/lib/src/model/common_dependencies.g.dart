// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'common_dependencies.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CommonDependenciesCWProxy {
  CommonDependencies refs(List<String> refs);

  CommonDependencies asOf(DateTime? asOf);

  CommonDependencies generatedAt(DateTime? generatedAt);

  CommonDependencies dependencies(
    List<CommonDependenciesDependenciesInner> dependencies,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependencies(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependencies(...).copyWith(id: 12, name: "My name")
  /// ```
  CommonDependencies call({
    List<String> refs,
    DateTime? asOf,
    DateTime? generatedAt,
    List<CommonDependenciesDependenciesInner> dependencies,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCommonDependencies.copyWith(...)` or call `instanceOfCommonDependencies.copyWith.fieldName(value)` for a single field.
class _$CommonDependenciesCWProxyImpl implements _$CommonDependenciesCWProxy {
  const _$CommonDependenciesCWProxyImpl(this._value);

  final CommonDependencies _value;

  @override
  CommonDependencies refs(List<String> refs) => call(refs: refs);

  @override
  CommonDependencies asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  CommonDependencies generatedAt(DateTime? generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  CommonDependencies dependencies(
    List<CommonDependenciesDependenciesInner> dependencies,
  ) => call(dependencies: dependencies);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependencies(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependencies(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CommonDependencies call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? dependencies = const $CopyWithPlaceholder(),
  }) {
    return CommonDependencies(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      generatedAt: generatedAt == const $CopyWithPlaceholder()
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime?,
      dependencies:
          dependencies == const $CopyWithPlaceholder() || dependencies == null
          ? _value.dependencies
          // ignore: cast_nullable_to_non_nullable
          : dependencies as List<CommonDependenciesDependenciesInner>,
    );
  }
}

extension $CommonDependenciesCopyWith on CommonDependencies {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCommonDependencies.copyWith(...)` or `instanceOfCommonDependencies.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CommonDependenciesCWProxy get copyWith =>
      _$CommonDependenciesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommonDependencies _$CommonDependenciesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CommonDependencies', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['refs', 'dependencies']);
      final val = CommonDependencies(
        refs: $checkedConvert(
          'refs',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        dependencies: $checkedConvert(
          'dependencies',
          (v) => (v as List<dynamic>)
              .map(
                (e) => CommonDependenciesDependenciesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CommonDependenciesToJson(CommonDependencies instance) =>
    <String, dynamic>{
      'refs': instance.refs,
      'asOf': ?instance.asOf?.toIso8601String(),
      'generatedAt': ?instance.generatedAt?.toIso8601String(),
      'dependencies': instance.dependencies.map((e) => e.toJson()).toList(),
    };
