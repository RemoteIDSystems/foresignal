// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_note_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystNotePatchCWProxy {
  AnalystNotePatch status(AnalystNotePatchStatusEnum status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNotePatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNotePatch(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystNotePatch call({AnalystNotePatchStatusEnum status});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystNotePatch.copyWith(...)` or call `instanceOfAnalystNotePatch.copyWith.fieldName(value)` for a single field.
class _$AnalystNotePatchCWProxyImpl implements _$AnalystNotePatchCWProxy {
  const _$AnalystNotePatchCWProxyImpl(this._value);

  final AnalystNotePatch _value;

  @override
  AnalystNotePatch status(AnalystNotePatchStatusEnum status) =>
      call(status: status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystNotePatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystNotePatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystNotePatch call({Object? status = const $CopyWithPlaceholder()}) {
    return AnalystNotePatch(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AnalystNotePatchStatusEnum,
    );
  }
}

extension $AnalystNotePatchCopyWith on AnalystNotePatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystNotePatch.copyWith(...)` or `instanceOfAnalystNotePatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystNotePatchCWProxy get copyWith => _$AnalystNotePatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystNotePatch _$AnalystNotePatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystNotePatch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = AnalystNotePatch(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$AnalystNotePatchStatusEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalystNotePatchToJson(AnalystNotePatch instance) =>
    <String, dynamic>{
      'status': _$AnalystNotePatchStatusEnumEnumMap[instance.status]!,
    };

const _$AnalystNotePatchStatusEnumEnumMap = {
  AnalystNotePatchStatusEnum.new_: 'new',
  AnalystNotePatchStatusEnum.read: 'read',
  AnalystNotePatchStatusEnum.dismissed: 'dismissed',
};
