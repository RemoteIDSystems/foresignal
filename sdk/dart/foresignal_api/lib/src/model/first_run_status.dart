//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'first_run_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FirstRunStatus {
  /// Returns a new [FirstRunStatus] instance.
  FirstRunStatus({

    required  this.firstRun,

    required  this.hasUsers,

     this.smtpConfigured,

     this.sourcesEnabled,

     this.apiVersion,
  });

      /// True when no users exist yet
  @JsonKey(
    
    name: r'firstRun',
    required: true,
    includeIfNull: false,
  )


  final bool firstRun;



  @JsonKey(
    
    name: r'hasUsers',
    required: true,
    includeIfNull: false,
  )


  final bool hasUsers;



  @JsonKey(
    
    name: r'smtpConfigured',
    required: false,
    includeIfNull: false,
  )


  final bool? smtpConfigured;



      /// How many sources are in the collection set
  @JsonKey(
    
    name: r'sourcesEnabled',
    required: false,
    includeIfNull: false,
  )


  final int? sourcesEnabled;



  @JsonKey(
    
    name: r'apiVersion',
    required: false,
    includeIfNull: false,
  )


  final String? apiVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FirstRunStatus &&
      other.firstRun == firstRun &&
      other.hasUsers == hasUsers &&
      other.smtpConfigured == smtpConfigured &&
      other.sourcesEnabled == sourcesEnabled &&
      other.apiVersion == apiVersion;

    @override
    int get hashCode =>
        firstRun.hashCode +
        hasUsers.hashCode +
        smtpConfigured.hashCode +
        sourcesEnabled.hashCode +
        apiVersion.hashCode;

  factory FirstRunStatus.fromJson(Map<String, dynamic> json) => _$FirstRunStatusFromJson(json);

  Map<String, dynamic> toJson() => _$FirstRunStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

