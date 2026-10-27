// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_egress_assignment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceEgressAssignmentCWProxy {
  SourceEgressAssignment routeId(String? routeId);

  SourceEgressAssignment excludeFromRotation(bool? excludeFromRotation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceEgressAssignment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceEgressAssignment(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceEgressAssignment call({String? routeId, bool? excludeFromRotation});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceEgressAssignment.copyWith(...)` or call `instanceOfSourceEgressAssignment.copyWith.fieldName(value)` for a single field.
class _$SourceEgressAssignmentCWProxyImpl
    implements _$SourceEgressAssignmentCWProxy {
  const _$SourceEgressAssignmentCWProxyImpl(this._value);

  final SourceEgressAssignment _value;

  @override
  SourceEgressAssignment routeId(String? routeId) => call(routeId: routeId);

  @override
  SourceEgressAssignment excludeFromRotation(bool? excludeFromRotation) =>
      call(excludeFromRotation: excludeFromRotation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceEgressAssignment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceEgressAssignment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceEgressAssignment call({
    Object? routeId = const $CopyWithPlaceholder(),
    Object? excludeFromRotation = const $CopyWithPlaceholder(),
  }) {
    return SourceEgressAssignment(
      routeId: routeId == const $CopyWithPlaceholder()
          ? _value.routeId
          // ignore: cast_nullable_to_non_nullable
          : routeId as String?,
      excludeFromRotation: excludeFromRotation == const $CopyWithPlaceholder()
          ? _value.excludeFromRotation
          // ignore: cast_nullable_to_non_nullable
          : excludeFromRotation as bool?,
    );
  }
}

extension $SourceEgressAssignmentCopyWith on SourceEgressAssignment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceEgressAssignment.copyWith(...)` or `instanceOfSourceEgressAssignment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceEgressAssignmentCWProxy get copyWith =>
      _$SourceEgressAssignmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceEgressAssignment _$SourceEgressAssignmentFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SourceEgressAssignment', json, ($checkedConvert) {
  final val = SourceEgressAssignment(
    routeId: $checkedConvert('routeId', (v) => v as String?),
    excludeFromRotation: $checkedConvert(
      'excludeFromRotation',
      (v) => v as bool? ?? false,
    ),
  );
  return val;
});

Map<String, dynamic> _$SourceEgressAssignmentToJson(
  SourceEgressAssignment instance,
) => <String, dynamic>{
  'routeId': ?instance.routeId,
  'excludeFromRotation': ?instance.excludeFromRotation,
};
