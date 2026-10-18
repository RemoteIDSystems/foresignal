//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_source_pull.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalSourcePull {
  /// Returns a new [ExternalSourcePull] instance.
  ExternalSourcePull({

     this.cursor,

     this.lastPollAt,

     this.lastError,
  });

  @JsonKey(
    
    name: r'cursor',
    required: false,
    includeIfNull: false,
  )


  final String? cursor;



  @JsonKey(
    
    name: r'lastPollAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastPollAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalSourcePull &&
      other.cursor == cursor &&
      other.lastPollAt == lastPollAt &&
      other.lastError == lastError;

    @override
    int get hashCode =>
        (cursor == null ? 0 : cursor.hashCode) +
        (lastPollAt == null ? 0 : lastPollAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode);

  factory ExternalSourcePull.fromJson(Map<String, dynamic> json) => _$ExternalSourcePullFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalSourcePullToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

