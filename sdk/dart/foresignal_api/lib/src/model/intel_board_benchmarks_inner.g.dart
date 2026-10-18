// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_benchmarks_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardBenchmarksInnerCWProxy {
  IntelBoardBenchmarksInner component(String component);

  IntelBoardBenchmarksInner status(IntelBoardBenchmarksInnerStatusEnum status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardBenchmarksInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardBenchmarksInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardBenchmarksInner call({
    String component,
    IntelBoardBenchmarksInnerStatusEnum status,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardBenchmarksInner.copyWith(...)` or call `instanceOfIntelBoardBenchmarksInner.copyWith.fieldName(value)` for a single field.
class _$IntelBoardBenchmarksInnerCWProxyImpl
    implements _$IntelBoardBenchmarksInnerCWProxy {
  const _$IntelBoardBenchmarksInnerCWProxyImpl(this._value);

  final IntelBoardBenchmarksInner _value;

  @override
  IntelBoardBenchmarksInner component(String component) =>
      call(component: component);

  @override
  IntelBoardBenchmarksInner status(
    IntelBoardBenchmarksInnerStatusEnum status,
  ) => call(status: status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardBenchmarksInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardBenchmarksInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardBenchmarksInner call({
    Object? component = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardBenchmarksInner(
      component: component == const $CopyWithPlaceholder() || component == null
          ? _value.component
          // ignore: cast_nullable_to_non_nullable
          : component as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as IntelBoardBenchmarksInnerStatusEnum,
    );
  }
}

extension $IntelBoardBenchmarksInnerCopyWith on IntelBoardBenchmarksInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardBenchmarksInner.copyWith(...)` or `instanceOfIntelBoardBenchmarksInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardBenchmarksInnerCWProxy get copyWith =>
      _$IntelBoardBenchmarksInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardBenchmarksInner _$IntelBoardBenchmarksInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardBenchmarksInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['component', 'status']);
  final val = IntelBoardBenchmarksInner(
    component: $checkedConvert('component', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$IntelBoardBenchmarksInnerStatusEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardBenchmarksInnerToJson(
  IntelBoardBenchmarksInner instance,
) => <String, dynamic>{
  'component': instance.component,
  'status': _$IntelBoardBenchmarksInnerStatusEnumEnumMap[instance.status]!,
};

const _$IntelBoardBenchmarksInnerStatusEnumEnumMap = {
  IntelBoardBenchmarksInnerStatusEnum.pass: 'pass',
  IntelBoardBenchmarksInnerStatusEnum.warn: 'warn',
  IntelBoardBenchmarksInnerStatusEnum.fail: 'fail',
  IntelBoardBenchmarksInnerStatusEnum.unknown: 'unknown',
};
