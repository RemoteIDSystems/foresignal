// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraStatusCWProxy {
  InfraStatus built(bool built);

  InfraStatus building(bool building);

  InfraStatus lastBuild(InfraBuild? lastBuild);

  InfraStatus nodesByType(Map<String, int>? nodesByType);

  InfraStatus osmExtractDate(DateTime? osmExtractDate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraStatus call({
    bool built,
    bool building,
    InfraBuild? lastBuild,
    Map<String, int>? nodesByType,
    DateTime? osmExtractDate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraStatus.copyWith(...)` or call `instanceOfInfraStatus.copyWith.fieldName(value)` for a single field.
class _$InfraStatusCWProxyImpl implements _$InfraStatusCWProxy {
  const _$InfraStatusCWProxyImpl(this._value);

  final InfraStatus _value;

  @override
  InfraStatus built(bool built) => call(built: built);

  @override
  InfraStatus building(bool building) => call(building: building);

  @override
  InfraStatus lastBuild(InfraBuild? lastBuild) => call(lastBuild: lastBuild);

  @override
  InfraStatus nodesByType(Map<String, int>? nodesByType) =>
      call(nodesByType: nodesByType);

  @override
  InfraStatus osmExtractDate(DateTime? osmExtractDate) =>
      call(osmExtractDate: osmExtractDate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraStatus call({
    Object? built = const $CopyWithPlaceholder(),
    Object? building = const $CopyWithPlaceholder(),
    Object? lastBuild = const $CopyWithPlaceholder(),
    Object? nodesByType = const $CopyWithPlaceholder(),
    Object? osmExtractDate = const $CopyWithPlaceholder(),
  }) {
    return InfraStatus(
      built: built == const $CopyWithPlaceholder() || built == null
          ? _value.built
          // ignore: cast_nullable_to_non_nullable
          : built as bool,
      building: building == const $CopyWithPlaceholder() || building == null
          ? _value.building
          // ignore: cast_nullable_to_non_nullable
          : building as bool,
      lastBuild: lastBuild == const $CopyWithPlaceholder()
          ? _value.lastBuild
          // ignore: cast_nullable_to_non_nullable
          : lastBuild as InfraBuild?,
      nodesByType: nodesByType == const $CopyWithPlaceholder()
          ? _value.nodesByType
          // ignore: cast_nullable_to_non_nullable
          : nodesByType as Map<String, int>?,
      osmExtractDate: osmExtractDate == const $CopyWithPlaceholder()
          ? _value.osmExtractDate
          // ignore: cast_nullable_to_non_nullable
          : osmExtractDate as DateTime?,
    );
  }
}

extension $InfraStatusCopyWith on InfraStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraStatus.copyWith(...)` or `instanceOfInfraStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraStatusCWProxy get copyWith => _$InfraStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraStatus _$InfraStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['built', 'building']);
      final val = InfraStatus(
        built: $checkedConvert('built', (v) => v as bool),
        building: $checkedConvert('building', (v) => v as bool),
        lastBuild: $checkedConvert(
          'lastBuild',
          (v) =>
              v == null ? null : InfraBuild.fromJson(v as Map<String, dynamic>),
        ),
        nodesByType: $checkedConvert(
          'nodesByType',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        osmExtractDate: $checkedConvert(
          'osmExtractDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InfraStatusToJson(InfraStatus instance) =>
    <String, dynamic>{
      'built': instance.built,
      'building': instance.building,
      'lastBuild': ?instance.lastBuild?.toJson(),
      'nodesByType': ?instance.nodesByType,
      'osmExtractDate': ?instance.osmExtractDate?.toIso8601String(),
    };
